package com.tenxi.exception;

import com.tenxi.decoder.FeignErrorDecoder;
import com.tenxi.enums.ErrorCode;

public class BusinessException extends BaseException {
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
