package com.tenxi.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenxi.notification.entity.dto.NotificationTypeAddDTO;
import com.tenxi.notification.entity.po.NotificationType;

public interface NotificationTypeService extends IService<NotificationType> {
    String addNotificationType(NotificationTypeAddDTO notificationType);
}
