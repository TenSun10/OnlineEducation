package com.tenxi.handler;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.tenxi.exception.BusinessException;
import com.tenxi.utils.RestBean;
import feign.FeignException;
import lombok.extern.java.Log;
import net.sf.jsqlparser.util.validation.ValidationException;
import org.springframework.dao.DataAccessException;
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
@Log
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 参数校验异常全局处理
     * @param e
     * @return
     */
    @ExceptionHandler(ValidationException.class)
    public RestBean<String> validationException(ValidationException e) {
        log.warning("参数校验失败:" + e.getMessage());
        return RestBean.failure(400, "参数校验失败" + e.getMessage());
    }

    /**
     * 请求参数异常全局处理
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RestBean<String> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ":" + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warning("请求参数错误:" + msg);
        return RestBean.failure(400, "请求参数错误:" + msg);
    }

    /**
     * 业务异常全局处理
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public RestBean<String> businessException(BusinessException e) {
        log.warning("业务异常:" + e.getMessage());
        return RestBean.failure(e.getCode(), e.getMessage());
    }

    /**
     * 数据库访问异常全局处理
     * @param e
     * @return
     */
    @ExceptionHandler(DataAccessException.class)
    public RestBean<String> dataAccessException(DataAccessException e) {
        log.warning("数据库访问异常:" + e.getMessage());
        return RestBean.failure(500, "数据库操作失败");
    }

    /**
     * 服务之间调用异常全局处理
     * @param e
     * @return
     */
    @ExceptionHandler(FeignException.class)
    public RestBean<String> feignException(FeignException e) {
        log.warning("服务调用异常: status = " + e.status() + ", message = " + e.getMessage());
        return RestBean.failure(503, "服务暂时不可用");
    }

    /**
     * 除去以上异常之外的所有异常的统一全局处理
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public RestBean<String> exception(Exception e) {
        log.warning("系统异常:" + e.getMessage());
        return RestBean.failure(500, e.getMessage());
    }
}
