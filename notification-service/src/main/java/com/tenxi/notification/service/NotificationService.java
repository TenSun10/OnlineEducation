package com.tenxi.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenxi.notification.entity.po.Notification;
import com.tenxi.notification.entity.vo.NotificationVO;
import com.tenxi.utils.RestBean;

public interface NotificationService extends IService<Notification> {
    RestBean<String> markAsRead(Long id);

    RestBean<NotificationVO> getNotificationVOById(Long id);
}
