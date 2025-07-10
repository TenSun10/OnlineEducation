package com.tenxi.notification.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationVO {
    private Long id;
    private String notificationType;
    private String notificationContent;
    private Long pusherId;
    private String pusherName;
    private String link;
    private Integer isRead;
    private LocalDateTime createTime;

}
