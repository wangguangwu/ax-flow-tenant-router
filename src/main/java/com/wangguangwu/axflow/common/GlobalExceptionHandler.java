package com.wangguangwu.axflow.common;

import com.wangguangwu.axflow.dto.ApiResult;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常捕获器
 * <p>
 * 将所有异常统一转成 {@link ApiResult}
 *
 * @author wangguangwu
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 参数校验失败（JSR-303，@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ":" + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", msg);
        return ApiResult.error(400, msg);
    }

    /**
     * 参数绑定失败（普通表单对象绑定）
     */
    @ExceptionHandler(BindException.class)
    public ApiResult<?> handleBindException(BindException ex) {
        String msg = ex.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", msg);
        return ApiResult.error(400, msg);
    }

    /**
     * 单参数校验失败（@Validated 参数）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<?> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ":" + v.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("单参数校验失败: {}", msg);
        return ApiResult.error(400, msg);
    }

    /**
     * 请求体 JSON 解析失败
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("请求体解析失败", ex);
        return ApiResult.error(400, "请求体解析失败: " + ex.getMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResult<?> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return ApiResult.error(400, ex.getMessage());
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<?> handleException(Exception ex) {
        log.error("系统内部错误", ex);
        return ApiResult.error(400, "系统内部错误: " + ex.getMessage());
    }
}
