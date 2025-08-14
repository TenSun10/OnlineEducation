package com.tenxi.exception;

import com.tenxi.enums.ErrorCode;

public class PayProcessException extends BaseException{
    public PayProcessException(ErrorCode errorCode) {
        super(errorCode);
    }
}
