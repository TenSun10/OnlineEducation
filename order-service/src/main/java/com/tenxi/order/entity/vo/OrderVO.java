package com.tenxi.order.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderVO {
    private Long id;
    private Long userId;
    private Long courseId;
    private String courseName;
    private String courseDes;
    private LocalDateTime expireTime;
    private Integer status;
    //显示创建订单时的价格
    private float totalFee;
    //显示现在课程的价格
    private float nowFee;
}
