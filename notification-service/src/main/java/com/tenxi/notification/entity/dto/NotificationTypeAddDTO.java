package com.tenxi.notification.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationTypeAddDTO {
    private String name;
    private String template;
    private String defaultChannel;
    private int isEnabled;
}
