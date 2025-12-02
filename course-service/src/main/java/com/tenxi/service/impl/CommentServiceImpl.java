package com.tenxi.service.impl;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenxi.client.AccountClient;
import com.tenxi.entity.dto.CommentAddDTO;
import com.tenxi.entity.po.Comment;
import com.tenxi.entity.po.Course;
import com.tenxi.entity.vo.AccountDetailVo;
import com.tenxi.entity.vo.CommentVO;
import com.tenxi.enums.ErrorCode;
import com.tenxi.exception.BusinessException;
import com.tenxi.mapper.CommentMapper;
import com.tenxi.mapper.CourseMapper;
import com.tenxi.service.CommentService;
import com.tenxi.utils.BaseContext;
import com.tenxi.utils.HmacSigner;
import com.tenxi.utils.RestBean;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Resource
    private CourseMapper courseMapper;

    @Resource
    private AccountClient accountClient;

    @Resource
    private RabbitTemplate rabbitTemplate;
    /**
     * 发布评论
     * @param commentAddDTO
     * @return
     */
    @Override
    public RestBean<Long> publishComment(CommentAddDTO commentAddDTO) {
        Long pusherId = BaseContext.getCurrentId();
        Long courseId = commentAddDTO.getCourseId();

        //1. 根据课程id查询到发布者的id
        LambdaQueryWrapper<Course> courseLambdaQueryWrapper = new LambdaQueryWrapper<>();
        courseLambdaQueryWrapper.eq(Course::getId, courseId);
        Course course = courseMapper.selectOne(courseLambdaQueryWrapper);
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        //2. 数据库存储数据
        Comment comment = new Comment();
        if (commentAddDTO.getParentId() == null) {
            comment.setParentId(0L);
        }
        BeanUtils.copyProperties(commentAddDTO, comment);

        comment.setCreateTime(LocalDateTime.now());
        comment.setUserId(pusherId);

        boolean save = save(comment);


        if (save) {
            Map<String , Object> comment_event = new HashMap<>();
            Map<String , Object> replay_event = new HashMap<>();

            //用于异步调用client的身份验证
            String signature = HmacSigner.sign(pusherId.toString());
            comment_event.put("X-User-Id", pusherId);
            comment_event.put("X-Signature", signature);

            replay_event.put("X-User-Id", pusherId);
            replay_event.put("X-Signature", signature);


            //判断为回复--另外需要发送回复的通知
            if (commentAddDTO.getParentId() != null){
                //判断为回复
                replay_event.put("course_id", courseId);
                replay_event.put("pusher_id", pusherId);
                Long parentId = commentAddDTO.getParentId();
                replay_event.put("parent_comment_id", commentAddDTO.getParentId());
                replay_event.put("event_type", "reply");
                Comment commentParent = getById(parentId);
                replay_event.put("receiver_id", commentParent.getUserId());
                replay_event.put("comment_id", comment.getId());
                log.info("发送回复通知...");
                rabbitTemplate.convertAndSend("online.direct", "online-education-notify", replay_event);

            }
            //都需要发送评论的通知
            comment_event.put("course_id", courseId);
            comment_event.put("pusher_id", pusherId);
            comment_event.put("event_type", "comment");
            comment_event.put("receiver_id", course.getPusherId()); //接受者的id（课程的发布者id）
            comment_event.put("comment_id", comment.getId());
            log.info("发送课程评论通知");
            rabbitTemplate.convertAndSend("online.direct", "online-education-notify", comment_event);

            return RestBean.successWithData(comment.getId());
        }else {
            throw new BusinessException(ErrorCode.COMMENT_PUBLISH_FAILED);
        }

    }

    /**
     * 根据id删除评论
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestBean<String> deleteCommentById(Long id) {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getId, id);
        Comment comment = getOne(queryWrapper);

        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }

        //1. 判断是否有权限删除评论-课程的发布者和评论的发布者可以删除
        LambdaQueryWrapper<Course> courseQueryWrapper = new LambdaQueryWrapper<>();
        courseQueryWrapper.eq(Course::getId, comment.getCourseId());
        Course course = courseMapper.selectOne(courseQueryWrapper);

        if (!Objects.equals(course.getPusherId(), userId) && !Objects.equals(comment.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_AUTH);
        }

        List<Long> commentIds = getAllChildrenCommentIds(id);
        commentIds.add(id);

        boolean remove = removeBatchByIds(commentIds);

        if (remove) {
            return RestBean.successWithMsg("评论删除成功");
        }else {
            throw new BusinessException(ErrorCode.SERVER_INNER_ERROR);
        }
    }

    /**
     * 根据评论id判断当前用户是否有操作权限
     * @param id
     * @return
     */
    @Override
    public RestBean<Boolean> judgeComment(Long id) {
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<Comment>();
        queryWrapper.eq(Comment::getId, id);
        Comment comment = getOne(queryWrapper);
        if (Objects.isNull(comment)) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }

        LambdaQueryWrapper<Course> courseQueryWrapper = new LambdaQueryWrapper<>();
        courseQueryWrapper.eq(Course::getId, comment.getCourseId());
        Course course = courseMapper.selectOne(courseQueryWrapper);


        if (!Objects.equals(course.getPusherId(), userId) && !Objects.equals(comment.getUserId(), userId)) {
            return RestBean.successWithData(false);
        }
        return RestBean.successWithData(true);
    }

    /**
     * 根据课程id查询课程的评论
     * @param id
     * @return
     */
    @Override
    public RestBean<List<CommentVO>> getCourseComments(Long id) {
        //1. 获取到该课程的所有评论
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getCourseId, id);
        List<Comment> commentList = getBaseMapper().selectList(queryWrapper);

        if (CollectionUtils.isEmpty(commentList)) {
            return RestBean.successWithData(Collections.emptyList());
        }

        // 2. 批量获取用户信息
        Set<Long> userIds = commentList.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());
        Map<Long, String> usernameMap = batchGetUsernames(userIds);

        //3. 构建评论树
        List<CommentVO> commentVOList = generateCommentsTree(commentList, usernameMap);
        return RestBean.successWithData(commentVOList);
    }

    /**
     * 批量查询生成id和用户名对应的Map
     * @param userIds
     * @return
     */
    private Map<Long, String> batchGetUsernames(Set<Long> userIds) {
        // 调用支持批量查询的接口
        List<Long> ids = userIds.stream().toList();
        List<AccountDetailVo> accounts = accountClient.batchQueryAccounts(ids).data();

        Map<Long, String> usernameMap = new HashMap<>();
        for (AccountDetailVo accountDetailVo : accounts) {
            usernameMap.put(accountDetailVo.getId(), accountDetailVo.getUsername());
        }
        return usernameMap;
    }

    /**
     * 生成评论树
     * @param commentList
     * @param usernameMap
     * @return
     */
    private List<CommentVO> generateCommentsTree(List<Comment> commentList, Map<Long, String> usernameMap) {
        //1. 构造除了顶层评论的分组结构
        Map<Long, List<Comment>> childrenMap = commentList.stream()
                .filter(comment -> comment.getParentId() != 0L)
                .collect(Collectors.groupingBy(Comment::getParentId));

        //2. 给每一个comment创造相应的VO
        Map<Long, CommentVO> voMap = commentList.stream()
                .collect(Collectors.toMap(
                        Comment::getId,
                        comment -> convertToVO(comment, usernameMap)
                ));

        //3. 构建包括子树结构
        List<CommentVO> result = new ArrayList<>();
        for (Comment comment : commentList) {
            if (comment.getParentId() == 0L) {
                CommentVO vo = voMap.get(comment.getId());
                vo.setComments(getChildren(vo.getId(), childrenMap, voMap));
                result.add(vo);
            }
        }

        return result;
    }

    /**
     * 递归生成子树
     * @param parentId
     * @param childrenMap
     * @param voMap
     * @return
     */
    private List<CommentVO> getChildren(Long parentId, Map<Long, List<Comment>> childrenMap, Map<Long, CommentVO> voMap) {
        List<Comment> childComments = childrenMap.getOrDefault(parentId, Collections.emptyList());

        if (childComments.isEmpty()) return Collections.emptyList();

        return childComments.stream()
                .map(comment -> {
                    CommentVO vo = voMap.get(comment.getId());
                    vo.setComments(getChildren(comment.getId(), childrenMap, voMap));
                    return vo;
                }).toList();
    }

    /**
     * 构造id和VO对应的Map
     * @param comment
     * @param usernameMap
     * @return
     */
    private CommentVO convertToVO(Comment comment, Map<Long, String> usernameMap) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        vo.setCommenterId(comment.getUserId());
        vo.setCommenterUsername(usernameMap.get(comment.getUserId()));
        vo.setComments(new ArrayList<>()); //初始化空列表
        return vo;
    }

    private List<Long> getAllChildrenCommentIds(Long parentId) {
        List<Long> allChildrenCommentIds = new ArrayList<>();
        Queue<Long> queue = new LinkedList<>();
        queue.add(parentId);

        while (!queue.isEmpty()) {
            Long id = queue.poll();
            LambdaQueryWrapper<Comment> commentLambdaQueryWrapper = new LambdaQueryWrapper<>();
            commentLambdaQueryWrapper.eq(Comment::getParentId, id);

            List<Comment> children = list(commentLambdaQueryWrapper);
            if (!CollectionUtils.isEmpty(children)) {
                for (Comment comment : children) {
                    allChildrenCommentIds.add(comment.getId());
                    queue.add(comment.getId());
                }
            }
        }

        return allChildrenCommentIds;
    }
}
