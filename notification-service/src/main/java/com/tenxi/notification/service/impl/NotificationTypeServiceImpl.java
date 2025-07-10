package com.tenxi.notification.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenxi.notification.entity.dto.NotificationTypeAddDTO;
import com.tenxi.notification.entity.po.NotificationType;
import com.tenxi.notification.mapper.NotificationTypeMapper;
import com.tenxi.notification.service.NotificationTypeService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationTypeServiceImpl extends ServiceImpl<NotificationTypeMapper, NotificationType> implements NotificationTypeService {
    public NotificationTypeServiceImpl() {
    }

    public String addNotificationType(NotificationTypeAddDTO notificationType) {
        NotificationType nt = new NotificationType();
        BeanUtils.copyProperties(notificationType, nt);
        nt.setCreateTime(LocalDateTime.now());
        boolean save = this.save(nt);
        return save ? null : "通知类型发布失败";
    }
}
