package com.tenxi.notification.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tenxi.enums.ErrorCode;
import com.tenxi.exception.BusinessException;
import com.tenxi.notification.client.AccountClient;
import com.tenxi.notification.client.CourseClient;
import com.tenxi.notification.entity.po.Notification;
import com.tenxi.notification.entity.po.NotificationType;
import com.tenxi.notification.entity.vo.AccountDetailVo;
import com.tenxi.notification.entity.vo.CourseSimpleVO;
import com.tenxi.notification.mapper.NotificationMapper;
import com.tenxi.notification.service.NotificationTypeService;
import com.tenxi.notification.service.WebSocketService;
import com.tenxi.utils.BaseContext;
import com.tenxi.utils.HmacSigner;
import jakarta.annotation.Resource;
import lombok.extern.java.Log;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
@Component
@RabbitListener(
        bindings = {@QueueBinding(
                value = @Queue(
                        name = "online.education.notify"
                ),
                exchange = @Exchange(
                        name = "online.direct"
                ),
                key = {"online-education-notify"}
        )}
)
public class NotifyListener {
    @Resource
    private NotificationMapper notificationMapper;

    @Resource
    private NotificationTypeService notificationTypeService;

    @Resource
    private AccountClient accountClient;

    @Resource
    private CourseClient courseClient;

    @Resource
    private WebSocketService webSocketService;

    // 通知类型常量
    private static final String COMMENT_TYPE = "comment";
    private static final String REPLY_TYPE = "reply";
    private static final String COURSE_UPDATE_TYPE = "course_update";

    @RabbitHandler
    public void handleNotificationEvent(Map<String, Object> event) throws JsonProcessingException {
        String eventType = (String) event.get("event_type");

        log.info("🎯 RabbitMQ收到消息，事件类型: " +  eventType);
        log.info("完整消息内容: " +  event);

        Long userId = (Long) event.get("X-User-Id");
        String signature = (String) event.get("X-Signature");

        if (userId != null && signature != null && HmacSigner.verify(userId.toString(), signature)) {
            // 设置到BaseContext
            BaseContext.setCurrentId(userId);
        }else {
            log.warning("消息中缺少有效的认证信息，无法设置上下文");
            return;
        }

        switch (eventType) {
            case COMMENT_TYPE:
                handleCommentEvent(event);
                break;
            case REPLY_TYPE:
                handleReplyEvent(event);
                break;
            case COURSE_UPDATE_TYPE:
                handleCourseUpdateEvent(event);
                break;
            default:
                log.warning("未知通知类型:" + eventType);
        }
    }

    private void handleCourseUpdateEvent(Map<String, Object> event) {
        //1. 获取到通知类型的详细信息
        LambdaQueryWrapper<NotificationType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NotificationType::getType, COURSE_UPDATE_TYPE);
        NotificationType type = notificationTypeService.getOne(queryWrapper);
        if (type == null) {
            log.warning("未找到匹配的通知类型");
            throw new BusinessException(ErrorCode.NOTIFY_TYPE_NOT_FOUND);
        }

        //2. 获取到课程的相关信息
        Long courseId = (Long) event.get("course_id");
        CourseSimpleVO course = courseClient.getCourse(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        //3. 获取到订阅了该课程的用户信息，向其发送通知
        List<Long> subscribers = courseClient.getCourseSubscribers(courseId);
        String content = String.format(type.getTemplate(), course.getTitle());

        subscribers.forEach(subscriber -> {
            try {
                createAndSendNotification(type.getId(), content, subscriber, course.getPusherId(), "/courses/update/" + courseId);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void handleReplyEvent(Map<String, Object> event) throws JsonProcessingException {
        //1. 获取到通知类型的详细信息
        LambdaQueryWrapper<NotificationType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NotificationType::getType, REPLY_TYPE);
        NotificationType type = notificationTypeService.getOne(queryWrapper);
        if (type == null) {
            log.warning("未找到匹配的通知类型");
            throw new BusinessException(ErrorCode.NOTIFY_TYPE_NOT_FOUND);
        }

        //2.1 从event中获取需要的信息
        Long courseId = (Long) event.get("course_id");
        Long pusherId = (Long) event.get("pusher_id");
        Long parentCommentId = (Long) event.get("parent_comment_id");
        Long commentId = (Long) event.get("comment_id");
        Long receiverId = (Long) event.get("receiver_id");

        //2.2 获取用户友好信息
        AccountDetailVo replier = accountClient.queryAccountById(pusherId).data();
        CourseSimpleVO course = courseClient.getCourse(courseId);

        //3. 生成通知的内容
        if (replier == null) {
            log.warning("无法获取回复者或课程信息" + "发送者" + pusherId);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        String content = String.format(type.getTemplate(),replier.getUsername() , course.getTitle());
        String link = String.format("/courses/%d/comments/%d?replyTo=%d",
                courseId, commentId, parentCommentId);

        createAndSendNotification(type.getId(), content, receiverId, pusherId, link);

    }

    private void handleCommentEvent(Map<String, Object> event) throws JsonProcessingException {
        //1. 获取通知类型
        LambdaQueryWrapper<NotificationType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NotificationType::getType, COMMENT_TYPE);
        NotificationType type = notificationTypeService.getOne(queryWrapper);
        if (type == null) {
            log.warning("未找到匹配的通知类型");
            throw new BusinessException(ErrorCode.NOTIFY_TYPE_NOT_FOUND);
        }

        //2. 获取event的信息生成相应的通知内容
        Long receiveId = (Long) event.get("receiver_id"); //课程的发布者
        Long courseId = (Long) event.get("course_id");
        CourseSimpleVO course = courseClient.getCourse(courseId);
        Long pusherId = (Long) event.get("pusher_id"); //评论的发布者
        AccountDetailVo account = accountClient.queryAccountById(pusherId).data();
        Long commentId = (Long) event.get("comment_id");//发布的评论的id

        //3. 发送和保存通知
        String link = String.format("/courses/%s/comments/%s", courseId, commentId);
        String content = String.format(type.getTemplate(), account.getUsername(), course.getTitle());
        createAndSendNotification(type.getId(), content, receiveId, pusherId, link);
    }

    private void createAndSendNotification(Long typeId, String content, Long receiverId, Long senderId, String link) throws JsonProcessingException {
        log.info("🎯 开始createAndSendNotification - 类型: " + typeId + ", 接收者: " + receiverId + "  , 内容: " + content);
        //1. 将通知存入数据库
        Notification notification = new Notification();
        notification.setTypeId(typeId);
        notification.setContent(content);
        notification.setReceiverId(receiverId);
        notification.setPusherId(senderId);
        notification.setLink(link);
        notification.setCreateTime(LocalDateTime.now());
        log.info("线程：" + Thread.currentThread().getId() + "将要向数据库插入notification");

        log.info("📝 准备插入数据库 - 线程: " +  Thread.currentThread().getId());
        notificationMapper.insert(notification);
        log.info("✅ 数据库插入完成 - 通知ID: " +  notification.getId());

        //2. 使用WebSocket发送给用户
        Map<String, Object> wsMessage = new HashMap<>();
        wsMessage.put("notification_id", notification.getId());
        wsMessage.put("content", content);
        wsMessage.put("type", "notification");
        wsMessage.put("link", link);

        log.info("准备向用户 " + receiverId + " 发送WebSocket通知，内容: " + content);
        try {
            webSocketService.sendToUser(receiverId, wsMessage);
            log.info("WebSocket发送请求完成，用户: "+ receiverId + ", 通知ID: " +  notification.getId());
        } catch (Exception e) {
            log.warning("WebSocket发送异常，用户: " + receiverId + ", 错误: " + e.getMessage());
        }
    }
}
