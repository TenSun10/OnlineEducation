package com.tenxi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * 用于实现逻辑过期的实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisData<T> {
    private LocalDateTime expireTime;
    private T data;
}
