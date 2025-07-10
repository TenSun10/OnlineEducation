package com.tenxi.notification.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tenxi.notification.client.AccountClient;
import com.tenxi.notification.client.CourseClient;
import com.tenxi.notification.entity.po.Notification;
import com.tenxi.notification.entity.po.NotificationType;
import com.tenxi.notification.entity.vo.AccountDetailVo;
import com.tenxi.notification.entity.vo.CourseSimpleVO;
import com.tenxi.notification.mapper.NotificationMapper;
import com.tenxi.notification.service.NotificationTypeService;
import com.tenxi.notification.service.WebSocketService;
import jakarta.annotation.Resource;
import lombok.extern.java.Log;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
    private RabbitTemplate rabbitTemplate;
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
            log.warning("未知错误：回复通知类型未配置");
            return;
        }

        //2. 获取到课程的相关信息
        Long courseId = (Long) event.get("course_id");
        CourseSimpleVO course = courseClient.getCourse(courseId);
        if (course == null) {
            log.warning("课程不存在:" + courseId);
            return;
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
            log.warning("未知错误：回复通知类型未配置");
            return;
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
            log.warning("无法获取回复者或课程信息");
            return;
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
            log.warning("未知错误");
            return;
        }

        //2. 获取event的信息生成相应的通知内容
        Long receiveId = (Long) event.get("receiver_id"); //课程的发布者
        Long courseId = (Long) event.get("course_id");
        CourseSimpleVO course = courseClient.getCourse(courseId);
        Long pusherId = (Long) event.get("pusher_id"); //评论的发布者
        AccountDetailVo account = accountClient.queryAccountById(pusherId).data();
        String commentId = (String) event.get("comment_id");//发布的评论的id

        //3. 发送和保存通知
        String link = String.format("/courses/%s/comments/%s", courseId, commentId);
        String content = String.format(type.getTemplate(), account.getUsername(), course.getTitle());
        createAndSendNotification(type.getId(), content, receiveId, pusherId, link);
    }

    private void createAndSendNotification(Long typeId, String content, Long receiverId, Long senderId, String link) throws JsonProcessingException {
        //1. 将通知存入数据库
        Notification notification = new Notification();
        notification.setTypeId(typeId);
        notification.setContent(content);
        notification.setReceiverId(receiverId);
        notification.setPusherId(senderId);
        notification.setLink(link);
        notification.setCreatedTime(LocalDateTime.now());
        notificationMapper.insert(notification);

        //2. 使用WebSocket发送给用户
        Map<String, Object> wsMessage = new HashMap<>();
        wsMessage.put("notification_id", notification.getId());
        wsMessage.put("content", content);
        wsMessage.put("type", "notification");
        wsMessage.put("link", link);
        webSocketService.sendToUser(receiverId, wsMessage);
    }
}
