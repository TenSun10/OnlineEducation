package com.tenxi.utils;

import com.alibaba.fastjson.JSON;
import org.slf4j.MDC;

import java.util.Optional;

public record RestBean<T> (int code, T data, String msg, long id){
    public static <T> RestBean<T> successWithData(T data){
        return new RestBean<>(200, data, "请求成功", requestId());
    }

    public static <T> RestBean<T> successWithMsg(String msg){
        return new RestBean<>(200, null, msg, requestId());
    }

    public static <T> RestBean<T> failure(int code, String msg){
        return new RestBean<>(code, null, msg, requestId());
    }

    public static <T> RestBean<T> unauthorized(String msg){
        return failure(401, msg);
    }
    public static <T> RestBean<T> forbidden(String msg){
        return failure(403, msg);
    }

    public String asJsonString() {
        return JSON.toJSONString(this);
    }


    private static long requestId(){
        String requestId = Optional.ofNullable(MDC.get("reqId")).orElse("0");
        return Long.parseLong(requestId);
    }
}
