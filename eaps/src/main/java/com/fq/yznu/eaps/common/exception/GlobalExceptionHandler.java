package com.fq.yznu.eaps.common.exception;

import com.fq.yznu.eaps.common.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseResult<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return ResponseResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseResult<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = getBindingResultMessage(bindingResult);
        log.error("参数校验失败: {}", message, e);
        return ResponseResult.validateFailed(message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseResult<Void> handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = getBindingResultMessage(bindingResult);
        log.error("参数绑定失败: {}", message, e);
        return ResponseResult.validateFailed(message);
    }

    /**
     * 处理认证失败异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseResult<Void> handleBadCredentialsException(BadCredentialsException e) {
        log.error("认证失败: {}", e.getMessage(), e);
        return ResponseResult.unauthorized("用户名或密码错误");
    }

    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseResult<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.error("访问拒绝: {}", e.getMessage(), e);
        return ResponseResult.forbidden("没有权限访问此资源");
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseResult<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return ResponseResult.error("服务器内部错误，请联系管理员");
    }

    /**
     * 从BindingResult中获取错误信息
     */
    private String getBindingResultMessage(BindingResult bindingResult) {
        StringBuilder sb = new StringBuilder();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append(fieldError.getField()).append(": ").append(fieldError.getDefaultMessage()).append(", ");
        }

        if (sb.length() > 0) {
            sb.delete(sb.length() - 2, sb.length());
        }

        return sb.toString();
    }
}