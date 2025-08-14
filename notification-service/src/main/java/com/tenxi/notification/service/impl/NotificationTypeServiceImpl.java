package com.tenxi.notification.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenxi.enums.ErrorCode;
import com.tenxi.exception.BusinessException;
import com.tenxi.notification.entity.dto.NotificationTypeAddDTO;
import com.tenxi.notification.entity.po.NotificationType;
import com.tenxi.notification.mapper.NotificationTypeMapper;
import com.tenxi.notification.service.NotificationTypeService;
import com.tenxi.utils.RestBean;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationTypeServiceImpl extends ServiceImpl<NotificationTypeMapper, NotificationType> implements NotificationTypeService {
    public NotificationTypeServiceImpl() {
    }

    public RestBean<String> addNotificationType(NotificationTypeAddDTO notificationType) {
        NotificationType nt = new NotificationType();
        BeanUtils.copyProperties(notificationType, nt);
        nt.setCreateTime(LocalDateTime.now());
        if(save(nt)) {
            return RestBean.successWithMsg("新增通知类型成功");
        }
        throw new BusinessException(ErrorCode.NOTIFY_TYPE_ADD_FAILED);
    }
}
