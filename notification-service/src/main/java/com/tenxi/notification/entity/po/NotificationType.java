package com.tenxi.notification.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationType {
    private Long id;
    private String name;
    private String template;
    private String type;
    private String defaultChannel;
    private int isEnabled;
    private LocalDateTime createTime;
}
