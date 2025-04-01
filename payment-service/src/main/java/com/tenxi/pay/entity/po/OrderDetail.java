package com.tenxi.pay.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long orderId;
    private Integer payType;
    private Float realFee;
    private LocalDateTime payTime;
    private Integer payStatus;
}
