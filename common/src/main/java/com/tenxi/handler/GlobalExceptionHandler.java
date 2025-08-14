package com.tenxi.handler;

import com.tenxi.enums.ErrorCode;
import com.tenxi.exception.BaseException;
import com.tenxi.exception.BusinessException;
import com.tenxi.utils.RestBean;
import feign.FeignException;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.util.validation.ValidationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 这个全局异常处理器会把SpringSecurity的异常处理覆盖
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有自定义基础异常
     */
    @ExceptionHandler(BaseException.class)
    public RestBean<String> handleBaseException(BaseException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("业务异常: [{}] {}", errorCode.getCode(), errorCode.getMessage());

        return RestBean.failure(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RestBean<String> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("参数校验失败: {}", errorMsg);

        return RestBean.failure(HttpStatus.BAD_REQUEST.value(), errorMsg);
    }


    /**
     * 处理所有未捕获异常
     */
    @ExceptionHandler(Exception.class)
    public RestBean<String> handleAllExceptions(Exception ex) {
        log.error("未处理异常: ", ex);
        return RestBean.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    }

    /**
     * 根据错误码确定HTTP状态
     */
    private HttpStatus determineHttpStatus(ErrorCode errorCode) {
        return switch (errorCode.getCode() / 1000) {
            case 1, 3 -> HttpStatus.BAD_REQUEST;       // 用户/订单错误
            case 4 -> HttpStatus.PAYMENT_REQUIRED;     // 支付错误
            case 6 -> HttpStatus.BAD_REQUEST;          // 参数错误
            default -> HttpStatus.INTERNAL_SERVER_ERROR; // 系统错误
        };
    }


}
