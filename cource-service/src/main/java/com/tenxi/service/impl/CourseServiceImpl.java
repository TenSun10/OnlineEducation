package com.tenxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenxi.client.AccountClient;
import com.tenxi.entity.po.Collect;
import com.tenxi.entity.vo.CourseSimpleVO;
import com.tenxi.mapper.CollectMapper;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


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
        course.setCreateAt(LocalDateTime.now());
        course.setUpdateAt(LocalDateTime.now());
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
     * 1.先将收藏的记录存储在redis中
     * 2.使用异步线程将其写入数据库
     * 目的:防止恶意点击使得数据库崩溃
     * @param id
     * @return
     */
    //TODO
    @Override
    public RestBean<CourseVO> collectCourse(Long id) {
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
        queryWrapper.ne(Course::getId, id);
        boolean remove = remove(queryWrapper);
        if (remove) {
            return null;
        }
        return "您没有删除本课程的权限";
    }

    /**
     *
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

        return updated ? null : "课程未发生变更";
    }

    /**
     * 将Course转换为CourseVO的方法封装
     * @param course
     * @return
     */
    private List<CourseVO> transVo(List<Course> course, Long userId) {
        return course.stream().map(item -> {
            CourseVO vo = new CourseVO();
            BeanUtils.copyProperties(item, vo);

            // 获取发布者邮箱
            RestBean<AccountDetailVo> restBean = accountClient.queryAccountById(item.getPusherId());
            AccountDetailVo accountDetailVo = restBean.data();
            vo.setPusherName(accountDetailVo.getEmail());
            vo.setPusherId(accountDetailVo.getId());

            // 获取课程关联的标签
            QueryWrapper<Tag> tagWrapper = new QueryWrapper<>();
            tagWrapper.inSql("id", "SELECT tag_id FROM course_tag WHERE course_id = " + item.getId());
            List<Tag> tags = tagMapper.selectList(tagWrapper);
            vo.setTags(tags);

            // 查找collect表，如果收藏了isCollect字段就为1，否则为0
            LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Collect::getCourseId, item.getId());
            wrapper.eq(Collect::getUserId, userId);

            Collect collect = collectMapper.selectOne(wrapper);
            if (collect == null) {
                vo.setIsCollect(0);
            } else {
                vo.setIsCollect(1);
            }

            return vo;
        }).toList();
    }
}
