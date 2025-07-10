package com.tenxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenxi.client.AccountClient;
import com.tenxi.entity.po.Collect;
import com.tenxi.entity.vo.CourseSimpleVO;
import com.tenxi.mapper.CollectMapper;
import com.tenxi.notification.entity.po.NotificationType;
import com.tenxi.notification.service.NotificationTypeService;
import com.tenxi.utils.BaseContext;
import com.tenxi.utils.RestBean;
import com.tenxi.entity.dto.CoursePublishDTO;
import com.tenxi.entity.po.Category;
import com.tenxi.entity.po.Course;
import com.tenxi.entity.po.Tag;
import com.tenxi.entity.vo.AccountDetailVo;
import com.tenxi.entity.vo.CourseVO;
import com.tenxi.mapper.CategoryMapper;
import com.tenxi.mapper.CourseMapper;
import com.tenxi.mapper.TagMapper;
import com.tenxi.service.CourseService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import static com.tenxi.common.CourseConstStr.CACHE_COURSE_COLLECT_HASH;
import static com.tenxi.common.CourseConstStr.CACHE_COURSE_COLLECT_SET;


//TODO 将用户上传的视频存储到视频库，并在数据库中存储获取视频的uri
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private TagMapper tagMapper;
    @Resource
    private AccountClient accountClient;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private CollectMapper collectMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private NotificationTypeService notificationTypeService;

    private static final String Hash_PREFIX = CACHE_COURSE_COLLECT_HASH;

    /**
     * 老师发布课程
     * @param dto
     * @return
     */
    @Override
    public String publishCourse(CoursePublishDTO dto) {
        //1.存储视频
        Long pusherId = BaseContext.getCurrentId();
        Course course = new Course();
        BeanUtils.copyProperties(dto, course);
        course.setPusherId(pusherId);
        save(course);

        //2.处理标签,选择利用消息队列异步处理
        rabbitTemplate.convertAndSend("online.edu.direct", "tag_save_op", course.getId().toString() + "," + dto.getTags());

        return null;
    }

    /**
     * 根据用户的输入内容查找相应的课程
     * 不确定是否为分类
     * @param des
     * @return
     */
    @Override
    public RestBean<List<CourseVO>> queryCourse(String des) {
        Long userId = BaseContext.getCurrentId();
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();

        //构造查询条件
        /*
        a.查询用户的选择是否为分类
        b.查询用的选择是否匹配tag
        c.查询用的选择和课程的标题和简介是否有一致的
         */

        // 1. 检查输入是否为分类
        List<Category> categories = categoryMapper.selectList(new QueryWrapper<Category>()
                .like("label", des));
        List<Long> categoryIds = new ArrayList<>();
        if (!categories.isEmpty()) {
            for (Category category : categories) {
                categoryIds.addAll(categoryMapper.getAllSubCategoryIds(category.getId()));
            }
        }

        // 检查输入是否为标签
        Tag tag = tagMapper.selectOne(new QueryWrapper<Tag>().eq("name", des));

        // 2. 构建动态查询条件
        queryWrapper.and(qw -> {
            boolean hasCondition = false;

            // 分类条件
            if (! categoryIds.isEmpty()) {
                //去重（在lambda表达式中，外部变量要么被设置为final，要么只赋值一次即effective final）
                List<Long> distinctCategoryIds = new ArrayList<>(categoryIds);
                distinctCategoryIds = distinctCategoryIds.stream().distinct().collect(Collectors.toList());
                qw.in("category_id", distinctCategoryIds);
                hasCondition = true;
            }

            // 标签条件
            if (tag != null) {
                if (hasCondition) {
                    qw.or(); // 添加OR连接
                }
                qw.apply("EXISTS (SELECT 1 FROM course_tag WHERE course_tag.course_id = course.id AND tag_id = {0})", tag.getId());
                hasCondition = true;
            }

            // 标题和简介的模糊查询条件（始终添加）
            if (hasCondition) {
                qw.or();
            }
            qw.nested(nestedQw ->
                    nestedQw.like("title", des)
                            .or()
                            .like("introduction", des)
            );
        });

        // 3. 执行查询
        List<Course> courses = courseMapper.selectList(queryWrapper);

        // 4. 处理结果，转换为VO
        List<CourseVO> res = transVo(courses, userId);

        return RestBean.successWithData(res);
    }

    /**
     * 根据用户传递的tag的id查询与之相关的课程
     * @param id
     * @return
     */
    @Override
    public RestBean<List<CourseVO>> getByTag(Long id) {
        Long userId = BaseContext.getCurrentId();
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        queryWrapper.apply("SELECT course_id FROM course_tag WHERE tag_id = " + id);
        List<Course> courses = courseMapper.selectList(queryWrapper);

        List<CourseVO> courseVOS = transVo(courses, userId);

        return RestBean.successWithData(courseVOS);
    }

    /**
     * 用户收藏课程
     * 1.先将收藏的记录存储在redis(使用set缓存）中，方便用户及时得到反馈
     * 2.将用户操作使用Hash结构存储，定时任务写入时看最后的值
     * 目的:防止恶意点击使得数据库崩溃
     * @param id
     * @return
     */
    @Override
    public String collectCourse(Long id) {
        Long currentId = BaseContext.getCurrentId();
        String key_hash = CACHE_COURSE_COLLECT_HASH + currentId;
        String key_set = CACHE_COURSE_COLLECT_SET + currentId;

        //1.修改用户操作的最后 获取当前操作类型（取反）
        String lastAction = (String) redisTemplate.opsForHash().get(key_hash, id.toString());
        int newAction = ("0".equals(lastAction) || lastAction == null) ? 1 : 0;

        //记录最新操作
        redisTemplate.opsForHash().put(key_hash, id.toString(), String.valueOf(newAction));
        redisTemplate.expire(key_hash, 1, TimeUnit.HOURS);

        //修改set中的值
        if(Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key_set, id.toString()))){
            redisTemplate.opsForSet().remove(key_set, id.toString());
        }else {
            redisTemplate.opsForSet().add(key_set, id.toString());
        }

        return null;
    }

    /**
     * 根据id查询课程相关信息
     * @param id
     * @return
     */
    @Override
    public RestBean<CourseVO> getCourseById(Long id) {
        Course course = getById(id);
        Long userId = BaseContext.getCurrentId();
        if (course != null) {
            List<CourseVO> courseVOS = transVo(List.of(course), userId);
            return RestBean.successWithData(courseVOS.get(0));
        }
        return RestBean.successWithMsg("该课程不存在");
    }

    /**
     * 主要用于其他模块调用这个方法获取Course的简单信息
     * @param id
     * @return
     */
    @Override
    public CourseSimpleVO getSimpleCourseById(Long id) {
        Course course = getById(id);
        if (course != null) {
            CourseSimpleVO vo = new CourseSimpleVO();
            BeanUtils.copyProperties(course, vo);
            return vo;
        }
        return null;
    }

    /**
     * 删除课程
     * @param id
     * @return
     */
    @Override
    public String deleteCourseById(Integer id) {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getPusherId, userId);
        queryWrapper.eq(Course::getId, id);
        boolean remove = remove(queryWrapper);
        if (remove) {
            return null;
        }
        return "您没有删除本课程的权限";
    }

    /**
     * 更新课程内容
     * @param dto
     * @return
     */
    @Override
    public String updateCourse(CoursePublishDTO dto, Long id) {
        // 验证ID不能为空
        if (id == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }

        LambdaUpdateWrapper<Course> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Course::getId, id);

        // 动态设置需要更新的字段
        if (StringUtils.hasText(dto.getTitle())) {
            updateWrapper.set(Course::getTitle, dto.getTitle());
        }
        if (StringUtils.hasText(dto.getIntroduction())) {
            updateWrapper.set(Course::getIntroduction, dto.getIntroduction());
        }
        if (Objects.nonNull(dto.getOriginPrice())) {
            updateWrapper.set(Course::getOriginPrice, dto.getOriginPrice());
        }
        if (Objects.nonNull(dto.getDiscountPrice())) {
            updateWrapper.set(Course::getDiscountPrice, dto.getDiscountPrice());
        }

        // 执行更新操作
        boolean updated = update(updateWrapper);
        if (updated) {
            //向所有订阅了该课程的用户发送站内通知
            Map<String, Object> event = new HashMap<>();
            event.put("event_type", "course_update");
            event.put("course_id", id);
            //使用消息队列异步发送信息
            rabbitTemplate.convertAndSend("online.direct","online-education-notify", event);
        }

        return updated ? null : "服务器内部错误，课程未发生变更";
    }

    /**
     * 获取课程的订阅用户的id
     * @param id
     * @return
     */
    @Override
    public List<Long> getCourseSubscribers(Long id) {
        return courseMapper.getCourseSubscribers(id);
    }

    /**
     * 将Course转换为CourseVO的方法封装
     * @param course
     * @return
     */
    private List<CourseVO> transVo(List<Course> course, Long userId) {
        if (course == null) {
            return null;
        }
        List<Long> publisherIds = course.stream().map(Course::getPusherId).distinct().toList();
        Map<Long, AccountDetailVo> accountMap = accountClient.batchQueryAccounts(publisherIds).data().stream()
                .collect(Collectors.toMap(AccountDetailVo::getId, Function.identity()));

        return course.stream().map(item -> {
            CourseVO vo = new CourseVO();
            BeanUtils.copyProperties(item, vo);

            // 获取发布者邮箱
            AccountDetailVo accountDetailVo = accountMap.get(item.getPusherId());
            if (accountDetailVo == null) {
                vo.setPusherName("未知用户");
            } else {
                vo.setPusherName(accountDetailVo.getUsername());
            }
            vo.setPusherId(userId);
            // 获取课程关联的标签
            QueryWrapper<Tag> tagWrapper = new QueryWrapper<>();
            tagWrapper.inSql("id", "SELECT tag_id FROM course_tag WHERE course_id = " + item.getId());
            List<Tag> tags = tagMapper.selectList(tagWrapper);
            vo.setTags(tags);

            boolean collect = isCollect(userId, item.getId());
            if (collect) {
                vo.setIsCollect(1);
            } else {
                vo.setIsCollect(0);
            }
            return vo;
        }).toList();
    }


    private boolean isCollect(Long userId, Long courseId) {
        //先从redis中查看用户是否收藏了该课程
        String key = CACHE_COURSE_COLLECT_SET + userId;
        if(Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, courseId.toString()))) {
            return true;
        }

        // 查找collect表，如果收藏了isCollect字段就为1，否则为0
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getCourseId, courseId);
        wrapper.eq(Collect::getUserId, userId);

        Collect collect = collectMapper.selectOne(wrapper);
        return collect != null;
    }


    /**
     * 定时任务 处理redis中课程收藏情况 存入或者删除相应的数据库数据
     */
    @Scheduled(fixedDelay = 60000)
    public void synCollectOperation() {
        Set<String> keys = redisTemplate.keys(Hash_PREFIX+"*");
        if(keys == null) return;

        for (String key : keys) {
            //处理每一个用户的收藏的改变
            Long userId = Long.parseLong(key.substring(Hash_PREFIX.length()));
            Map<Object, Object> operations = redisTemplate.opsForHash().entries(key);

            List<Long> toAdd = new ArrayList<>();
            List<Long> toRemove = new ArrayList<>();

            operations.forEach((courseStrId, action) -> {
                Long courseId = Long.parseLong(courseStrId.toString());
                if (action.equals("1")) {
                    toAdd.add(courseId);
                }else {
                    toRemove.add(courseId);
                }
            });

            if(!toAdd.isEmpty()) {
                collectMapper.batchInsertIgnore(userId, toAdd, LocalDateTime.now());
            }
            if(!toRemove.isEmpty()) {
                collectMapper.deleteByIds(toRemove);
            }

            //处理完之后删除这个键
            redisTemplate.delete(key);
        }
    }
}
