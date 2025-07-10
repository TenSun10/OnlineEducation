package com.tenxi.notification.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private Long id;
    private Long typeId;
    private Long pusherId;
    private Long receiverId;
    private String content;
    //link的作用是用户点击实现跳转
    private String link;
    private Integer isRead;
    private LocalDateTime createdTime;
}
