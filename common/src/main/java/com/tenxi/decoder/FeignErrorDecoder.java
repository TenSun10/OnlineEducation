package com.tenxi.decoder;

import com.tenxi.exception.BusinessException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

@Component
@Log
public class FeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 400 -> new BusinessException(400, "请求参数错误");
            case 401 -> new BusinessException(401, "未授权访问");
            case 404 -> new BusinessException(404, "服务不存在");
            case 500 -> new BusinessException(500, "服务内部错误");
            default -> new BusinessException(503, "服务不可用");
        };
    }
}
