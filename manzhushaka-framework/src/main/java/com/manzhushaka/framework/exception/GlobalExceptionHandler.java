package com.manzhushaka.framework.exception;

import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.common.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException exception) {
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage() == null ? error.getField() : error.getDefaultMessage())
            .orElse("参数校验失败");
        return ApiResponse.fail(400, message);
    }

    /**
     * 处理请求体反序列化异常，避免将底层解析细节直接暴露给前端。
     *
     * @param exception 请求体解析异常
     * @return 安全的参数错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return ApiResponse.fail(400, "请求参数格式不正确");
    }

    /**
     * 处理缺失请求参数的场景。
     *
     * @param exception 缺少参数异常
     * @return 安全的参数错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        return ApiResponse.fail(400, "请求参数不完整");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return ApiResponse.fail(500, "系统繁忙，请稍后再试");
    }
}
