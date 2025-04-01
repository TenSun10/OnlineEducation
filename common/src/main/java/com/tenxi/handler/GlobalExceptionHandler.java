package com.tenxi.handler;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.tenxi.utils.RestBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public RestBean<String> exception(Exception e) {
        if(e instanceof MybatisPlusException) {
            e.printStackTrace();
            return RestBean.failure(500, "数据库操作失败: " + e.getMessage());
        }
        e.printStackTrace();
        return RestBean.failure(500, "服务器内部错误");
    }
}
