package com.tenxi.exception;

import com.tenxi.enums.ErrorCode;


public class JwtException extends BaseException {
    public JwtException(ErrorCode errorCode) {
        super(errorCode);
    }
}
