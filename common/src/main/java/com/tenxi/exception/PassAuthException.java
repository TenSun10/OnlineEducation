package com.tenxi.exception;

import com.tenxi.enums.ErrorCode;

public class PassAuthException extends BaseException{
    public PassAuthException(ErrorCode errorCode) {
        super(errorCode);
    }
}
