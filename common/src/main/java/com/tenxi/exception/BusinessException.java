package com.tenxi.exception;

import com.tenxi.decoder.FeignErrorDecoder;
import com.tenxi.enums.ErrorCode;

public class BusinessException extends RuntimeException {
    private final int code;
    private final String msg;

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.msg = errorCode.getMessage();
    }

    public int getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
}
