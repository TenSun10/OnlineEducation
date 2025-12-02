package com.tenxi.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenxi.enums.ErrorCode;
import com.tenxi.exception.BusinessException;
import com.tenxi.notification.client.AccountClient;
import com.tenxi.notification.entity.po.Notification;
import com.tenxi.notification.entity.vo.NotificationVO;
import com.tenxi.notification.mapper.NotificationMapper;
import com.tenxi.notification.mapper.NotificationTypeMapper;
import com.tenxi.notification.service.NotificationService;
import com.tenxi.utils.BaseContext;
import com.tenxi.utils.RestBean;
import jakarta.annotation.Resource;
import lombok.extern.java.Log;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Log
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    @Resource
    private NotificationTypeMapper notificationTypeMapper;

    @Resource
    private AccountClient accountClient;


    @Override
    public RestBean<String> markAsRead(Long id) {
        //1. 判断需要修改的状态的通知是否存在以及接收者是否正确
        Long receiveId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<Notification>();
        queryWrapper.eq(Notification::getId, id);
        Notification notification = getOne(queryWrapper);

        if (notification == null) {
            log.warning("未知通知");
            throw new BusinessException(ErrorCode.NOTIFY_NOT_FOUND);
        }
        if (!notification.getReceiverId().equals(receiveId)) {
            log.warning("通知和接收者不匹配");
            throw new BusinessException(ErrorCode.SERVER_INNER_ERROR);
        }

        //2. 正确则修改状态
        notification.setIsRead(1);
        updateById(notification);

        return RestBean.successWithMsg("修改通知状态成功");
    }

    /**
     * 根据id查询通知详情信息
     * @param id
     * @return
     */
    @Override
    public RestBean<NotificationVO> getNotificationVOById(Long id) {
        Notification notification = getById(id);
        if (notification == null) {
            throw new BusinessException(ErrorCode.NOTIFY_NOT_FOUND);
        }
        NotificationVO notificationVO = new NotificationVO();
        BeanUtils.copyProperties(notification, notificationVO);

        String notificationType = notificationTypeMapper.selectById(notification.getTypeId()).getType();
        notificationVO.setNotificationType(notificationType);

        String username = accountClient.queryAccountById(notification.getPusherId()).data().getUsername();
        notificationVO.setPusherName(username);

        return RestBean.successWithData(notificationVO);
    }
}
