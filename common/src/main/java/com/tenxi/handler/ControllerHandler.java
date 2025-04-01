package com.tenxi.handler;

import com.tenxi.utils.RestBean;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ControllerHandler {
    public <T> RestBean<T> messageHandler(Supplier<String> action) {
        if(action.get() == null) {
            return RestBean.successWithMsg("请求成功");
        }else {
            return RestBean.failure(400, action.get());
        }
    }
}
