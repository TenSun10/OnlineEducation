package com.tenxi.enums;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(1001, "用户不存在"),
    COURSE_NOT_FOUND(2001, "课程不存在"),
    ORDER_EXPIRED(3001, "订单已过期"),
    ORDER_NOT_FOUND(3002, "订单不存在"),
    PAYMENT_FAILED(4001, "支付失败"),
    INSUFFICIENT_BALANCE(4002, "余额不足"),
    DATA_INTEGRITY_VIOLATION(5001, "数据完整性异常"),
    DATABASE_ERROR(5002, "数据库访问异常");


    private final int code;
    private final String message;

    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}
