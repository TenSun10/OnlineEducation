package com.tenxi.pay.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

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
    private Integer status;
    private LocalDateTime expireTime;
    //显示创建订单时的价格
    private float totalFee;
    //显示现在课程的价格
    private float nowFee;
}
