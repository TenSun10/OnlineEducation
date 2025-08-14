package com.tenxi.decoder;

import com.tenxi.enums.ErrorCode;
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
            case 400 -> new BusinessException(ErrorCode.FEIGN_PARAM_INVALID);
            case 401 -> new BusinessException(ErrorCode.FEIGN_AUTH_FAIL);
            case 404 -> new BusinessException(ErrorCode.FEIGN_SERVICE_NOT_FOUND);
            case 500 -> new BusinessException(ErrorCode.FEIGN_SERVICE_ERROR);
            default -> new BusinessException(ErrorCode.FEIGN_SERVICE_UNAVAILABLE);
        };
    }
}
