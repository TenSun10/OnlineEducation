package com.tenxi.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tenxi.notification.entity.po.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
