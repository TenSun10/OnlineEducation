package com.tenxi.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenxi.notification.entity.dto.NotificationTypeAddDTO;
import com.tenxi.notification.entity.po.NotificationType;
import com.tenxi.utils.RestBean;

public interface NotificationTypeService extends IService<NotificationType> {
    RestBean<String> addNotificationType(NotificationTypeAddDTO notificationType);
}
