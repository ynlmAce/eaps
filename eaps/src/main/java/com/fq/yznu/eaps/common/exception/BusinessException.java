package com.fq.yznu.eaps.common.exception;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {
    
    private Integer code;
    
    /**
     * 默认构造函数，状态码500
     *
     * @param message 异常信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
    
    /**
     * 构造函数
     *
     * @param message 异常信息
     * @param code    状态码
     */
    public BusinessException(String message, Integer code) {
        super(message);
        this.code = code;
    }
    
    /**
     * 获取状态码
     *
     * @return 状态码
     */
    public Integer getCode() {
        return code;
    }
} 