package com.cpt202.handler;

import com.cpt202.exception.BusinessException;
import com.cpt202.exception.RuleViolationException; // 确保导入了你写的异常类
import com.cpt202.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 * 统一拦截系统中抛出的异常，并封装成 Result 返回给前端。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 Module 8 的业务规则违规异常。
     * 当你在代码里 throw new RuleViolationException 时，会在这里被接住并返回 code: 0。
     */
    @ExceptionHandler(RuleViolationException.class)
    public Result<Void> handleRuleViolationException(RuleViolationException ex) {
        log.error("Module 8 业务规则拦截: {}", ex.getMessage());
        // Result.error 内部会自动将 code 设为 0
        return Result.error(ex.getMessage());
    }

    /**
     * 处理通用的业务异常。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        log.error("业务异常: {}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理 Spring Validation 参数校验异常。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Validation failed");
        log.error("参数校验失败: {}", message);
        return Result.error(message);
    }
}
