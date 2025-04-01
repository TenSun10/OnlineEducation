package com.tenxi.pay.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayCreateDTO {
    private Long orderId;
    private Integer payType;
    private Float realFee;

}
