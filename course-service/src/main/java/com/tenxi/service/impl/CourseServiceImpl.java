package com.tenxi.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenxi.client.AccountClient;
import com.tenxi.entity.es.CourseDocument;
import com.tenxi.entity.msg.CourseESSyncMessage;
import com.tenxi.entity.po.Collect;
import com.tenxi.entity.vo.CourseSimpleVO;
import com.tenxi.enums.ErrorCode;
import com.tenxi.exception.BusinessException;
import com.tenxi.mapper.*;
import com.tenxi.utils.AliyunOssUtil;
import com.tenxi.utils.BaseContext;
import com.tenxi.utils.HmacSigner;
import com.tenxi.utils.RestBean;
import com.tenxi.entity.dto.CoursePublishDTO;
import com.tenxi.entity.po.Category;
import com.tenxi.entity.po.Course;
import com.tenxi.entity.po.Tag;
import com.tenxi.entity.vo.AccountDetailVo;
import com.tenxi.entity.vo.CourseVO;
import com.tenxi.service.CourseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
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
    private CollectMapper collectMapper;


    @Resource
    private CourseSubscribeMapper subscribeMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private AliyunOssUtil ossUtil;

    @Resource
    private ElasticsearchOperations elasticsearchOperations;


    /**
     * 老师发布课程
     * @param dto
     * @return
     */
    @Override
    public RestBean<String> publishCourse(CoursePublishDTO dto) {
        //1. 判断文件是否存在
        if (dto.getVideoFile() == null || dto.getVideoFile().isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }


        //2.存储课程视频信息
        Long pusherId = BaseContext.getCurrentId();
        String videoUrl = null;
        String coverUrl = null;

        try {
            //1.向oss中存储 获取url
            videoUrl = ossUtil.uploadVideo(dto.getVideoFile(), null);

            if (dto.getImageFile() != null || ! dto.getImageFile().isEmpty()) {
                coverUrl = ossUtil.uploadCoverImage(dto.getImageFile(), null);
            }

            //2.向数据库中存储
            Course course = new Course();
            BeanUtils.copyProperties(dto, course);
            course.setPusherId(pusherId);
            course.setVideoUrl(videoUrl);
            course.setCoverImageUrl(coverUrl);
            course.setCreateTime(LocalDateTime.now());
            course.setUpdateTime(LocalDateTime.now());

            if (save(course)) {
                // 3.如果数据库保存成功，重命名OSS文件（加上课程ID）
                if (videoUrl != null) {
                    String newVideoUrl = ossUtil.renameFile(videoUrl, "videos", course.getId(), "video");
                    course.setVideoUrl(newVideoUrl);
                }
                if (coverUrl != null) {
                    String newCoverUrl = ossUtil.renameFile(coverUrl, "covers", course.getId(), "cover");
                    course.setCoverImageUrl(newCoverUrl);
                }

                // 更新课程记录
                updateById(course);

                //4.处理标签
                rabbitTemplate.convertAndSend("online.edu.direct",
                        "tag_save_op",
                        course.getId().toString() + "," + dto.getTags());

                //5.处理ES数据同步
                CourseESSyncMessage msg = new CourseESSyncMessage(course.getId(), CourseESSyncMessage.Operation.CREATE);
                rabbitTemplate.convertAndSend("online.edu.direct", "course_sync_op", msg);
            } else {
                // 数据库保存失败，删除已上传的OSS文件
                deleteOssFiles(videoUrl, coverUrl);
                throw new BusinessException(ErrorCode.COURSE_PUBLISH_FAILED);
            }
        }catch (Exception e){
            //无论任何异常都尝试从oss中删除文件
            deleteOssFiles(videoUrl, coverUrl);
            log.error("发布课程失败", e);
            throw new BusinessException(ErrorCode.COURSE_PUBLISH_FAILED);
        }

        return RestBean.successWithMsg("课程发布成功");
    }



    /**
     * 根据用户的输入内容查找相应的课程
     * 不确定是否为分类
     * @param des
     * @return
     */
    @Override
    public RestBean<List<CourseDocument>> queryCourse(String des) {
        try {
            Criteria criteria = new Criteria("title").contains(des)
                    .or("introduction").contains(des)
                    .or("category").contains(des)
                    .or("tag.tagName").contains(des);

            CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);

            List<CourseDocument> results = elasticsearchOperations.search(criteriaQuery, CourseDocument.class).stream()
                    .map(SearchHit::getContent)
                    .toList();
            return RestBean.successWithData(results);
        } catch (Exception e) {
            log.error("搜索课程失败, keyword: {}", des, e);
            throw new BusinessException(ErrorCode.SEARCH_FAILED);
        }


    }

    /**
     * 根据用户传递的tag的id查询与之相关的课程
     * @param id
     * @return
     */
    @Override
    public RestBean<List<CourseDocument>> getByTag(Long id) {
        try {
            Criteria criteria = new Criteria("tags.tagId").is(id.toString());
            CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);

            List<CourseDocument> documents = elasticsearchOperations.search(criteriaQuery, CourseDocument.class).stream()
                    .map(SearchHit::getContent)
                    .toList();

            return RestBean.successWithData(documents);
        }catch (Exception e){
            log.error("根据Tag的id搜索课程失败, id: {}",id , e);
            throw new BusinessException(ErrorCode.SEARCH_FAILED);
        }
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
        throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
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
        throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
    }

    /**
     * 删除课程
     * @param id
     * @return
     */
    @Override
    public RestBean<String> deleteCourseById(Long id) {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getPusherId, userId);
        queryWrapper.eq(Course::getId, id);
        Course course = getOne(queryWrapper);

        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        String videoUrl = course.getVideoUrl();
        String coverImageUrl = course.getCoverImageUrl();

        try {
            boolean remove = remove(queryWrapper);

            if (remove) {
                //1. 选择使用异步删除oss文件
                CompletableFuture.runAsync(() -> {
                    deleteOssFiles(videoUrl, coverImageUrl);
                });

                CourseESSyncMessage msg = new CourseESSyncMessage(course.getId(), CourseESSyncMessage.Operation.DELETE);
                rabbitTemplate.convertAndSend("online.edu.direct", "course_sync_op", msg);

                log.info("课程删除成功, courseId: {}, userId: {}", id, userId);
                return RestBean.successWithMsg("删除课程成功");
            }else {
                throw new BusinessException(ErrorCode.COURSE_DEL_FAILED);
            }
        }catch (Exception e){
            log.error("删除课程失败, courseId: {}", id, e);
            throw new BusinessException(ErrorCode.COURSE_DEL_FAILED);
        }

    }

    /**
     * 更新课程内容
     * @param dto
     * @return
     */
    @Override
    public RestBean<String> updateCourse(CoursePublishDTO dto, Long id) {
        // 验证ID不能为空
        if (id == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }

        // 1. 查询现有课程
        Course existingCourse = getById(id);
        if (existingCourse == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        // 2. 验证权限：只有课程发布者可以更新
        Long currentUserId = BaseContext.getCurrentId();
        if (!existingCourse.getPusherId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.COURSE_NOT_AUTH);
        }

        String oldVideoUrl = existingCourse.getVideoUrl();
        String oldCoverUrl = existingCourse.getCoverImageUrl();
        String newVideoUrl = null;
        String newCoverUrl = null;

        try {
            // 3. 处理视频文件更新
            if (Boolean.TRUE.equals(dto.getUpdateVideo()) && dto.getVideoFile() != null
                    && !dto.getVideoFile().isEmpty()) {
                newVideoUrl = ossUtil.uploadVideo(dto.getVideoFile(), id);
            }

            // 4. 处理封面图片更新
            if (Boolean.TRUE.equals(dto.getUpdateCover()) && dto.getImageFile() != null
                    && !dto.getImageFile().isEmpty()) {
                newCoverUrl = ossUtil.uploadCoverImage(dto.getImageFile(), id);
            }

            // 5. 构建更新条件
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
            if (dto.getCategoryId() != null) {
                updateWrapper.set(Course::getCategoryId, dto.getCategoryId());
            }

            // 更新文件URL
            if (newVideoUrl != null) {
                updateWrapper.set(Course::getVideoUrl, newVideoUrl);
            }
            if (newCoverUrl != null) {
                updateWrapper.set(Course::getCoverImageUrl, newCoverUrl);
            }

            updateWrapper.set(Course::getUpdateTime, LocalDateTime.now());

            // 6. 执行更新操作
            boolean updated = update(updateWrapper);

            if (updated) {
                // 7. 更新成功后删除旧文件
                if (newVideoUrl != null && oldVideoUrl != null) {
                    ossUtil.deleteFile(oldVideoUrl);
                }
                if (newCoverUrl != null && oldCoverUrl != null) {
                    ossUtil.deleteFile(oldCoverUrl);
                }

                //8. 课程视频更新发送通知
                if (Boolean.TRUE.equals(dto.getUpdateVideo())) {
                    sendCourseUpdateNotification(id, currentUserId);
                }

                // 同步到ES
                CourseESSyncMessage syncMessage = new CourseESSyncMessage(id, CourseESSyncMessage.Operation.UPDATE);
                rabbitTemplate.convertAndSend("online.edu.direct", "course_sync_op", syncMessage);

                return RestBean.successWithMsg("课程更新成功");
            }else {
                //数据库更新失败就 删除新文件
                deleteOssFiles(newVideoUrl, newCoverUrl);
                throw new BusinessException(ErrorCode.COURSE_UPDATE_FAILED);
            }
        } catch (Exception e) {
            deleteOssFiles(oldVideoUrl, oldCoverUrl);
            throw new BusinessException(ErrorCode.COURSE_UPDATE_FAILED);
        }

    }



    /**
     * 获取课程的订阅用户的id
     * @param courseId
     * @return
     */
    @Override
    public List<Long> getCourseSubscribers(Long courseId) {
        return subscribeMapper.getCourseSubscribers(courseId);
    }

    /**
     * 批量获取课程信息
     * @param courseIds
     * @return
     */
    @Override
    public List<CourseVO> getBatchCourses(List<Long> courseIds) {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Course::getId, courseIds);

        List<Course> courses = courseMapper.selectList(queryWrapper);
        Long userId = BaseContext.getCurrentId();

        return transVo(courses, userId);
    }


    private void deleteOssFiles(String videoUrl, String coverUrl) {
        try {
            if (videoUrl != null) {
                ossUtil.deleteFile(videoUrl);
            }
            if (coverUrl != null) {
                ossUtil.deleteFile(coverUrl);
            }
        } catch (Exception e) {
            // 不抛出异常，避免掩盖原始异常
            log.error("删除OSS文件失败", e);
        }
    }

    private boolean isCollect(Long userId, Long courseId) {
        // 查找collect表，如果收藏了isCollect字段就为1，否则为0
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getCourseId, courseId);
        wrapper.eq(Collect::getUserId, userId);

        Collect collect = collectMapper.selectOne(wrapper);
        return collect != null;
    }

    private void sendCourseUpdateNotification(Long courseId, Long userId) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("event_type", "course_update");
            event.put("course_id", courseId);
            event.put("update_time", LocalDateTime.now());

            String signature = HmacSigner.sign(userId.toString());
            event.put("X-Signature", signature);
            event.put("X-User-Id", userId);


            rabbitTemplate.convertAndSend("online.direct", "online-education-notify", event);
            log.info("课程更新通知发送成功, courseId: {}", courseId);
        } catch (Exception e) {
            log.error("发送课程更新通知失败, courseId: {}", courseId, e);
        }
    }

    //将Course转换为CourseVO的方法封装
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
}