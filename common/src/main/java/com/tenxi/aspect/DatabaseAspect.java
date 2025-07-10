package com.tenxi.aspect;

import com.tenxi.exception.BusinessException;
import lombok.extern.java.Log;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import com.tenxi.enums.ErrorCode;

/**
 * 使用Spring aop的切面类
 * 用于处理数据库相关带有注解@Transactional的方法的异常
 */
@Aspect
@Component
@Log
public class DatabaseAspect {

    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object handleDatabaseOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (DataIntegrityViolationException e) {
            log.warning("数据完整性约束违反: " + e.getMessage());
            throw new BusinessException(ErrorCode.DATA_INTEGRITY_VIOLATION);
        } catch (DataAccessException e) {
            log.warning("数据库访问异常: " + e.getMessage());
            throw new BusinessException(ErrorCode.DATABASE_ERROR);
        }
    }
}