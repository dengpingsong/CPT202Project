package com.cpt202.handler;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.NotFoundException;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.exception.UnauthorizedAccessException;
import com.cpt202.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 * 统一拦截系统中抛出的异常，并封装成 Result 返回给前端。
 */
@Slf4j
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理越权访问异常。
     * 当回调认证服务校验不通过时，会抛出此异常。
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public Result<Void> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        log.error("越权访问拦截: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public Result<Void> handleNotFoundException(NotFoundException ex) {
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(RuleViolationException.class)
    public Result<Void> handleRuleViolationException(RuleViolationException ex) {
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public Result<Void> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Validation failed");
        log.error("参数校验失败: {}", message);
        return Result.error(message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return Result.error(MessageConstants.DATA_INTEGRITY_VIOLATION);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Invalid request body", ex);
        return Result.error(MessageConstants.INVALID_REQUEST_BODY);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnexpectedException(Exception ex) {
        log.error("Unexpected server error", ex);
        return Result.error(MessageConstants.INTERNAL_SERVER_ERROR);
    }
}
