package com.fq.yznu.eaps.common;

import lombok.Data;

/**
 * 统一响应结果
 *
 * @param <T> 数据类型
 */
@Data
public class ResponseResult<T> {
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 状态信息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 成功结果，无数据
     *
     * @return 成功响应
     */
    public static <T> ResponseResult<T> success() {
        return success(null);
    }

    /**
     * 成功结果，有数据
     *
     * @param data 返回数据
     * @return 成功响应
     */
    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 失败结果
     *
     * @param code    状态码
     * @param message 失败信息
     * @return 失败响应
     */
    public static <T> ResponseResult<T> error(Integer code, String message) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败结果，默认状态码500
     *
     * @param message 失败信息
     * @return 失败响应
     */
    public static <T> ResponseResult<T> error(String message) {
        return error(500, message);
    }

    /**
     * 参数校验失败，状态码400
     *
     * @param message 失败信息
     * @return 失败响应
     */
    public static <T> ResponseResult<T> validateFailed(String message) {
        return error(400, message);
    }

    /**
     * 未授权，状态码401
     *
     * @param message 失败信息
     * @return 失败响应
     */
    public static <T> ResponseResult<T> unauthorized(String message) {
        return error(401, message);
    }

    /**
     * 禁止访问，状态码403
     *
     * @param message 失败信息
     * @return 失败响应
     */
    public static <T> ResponseResult<T> forbidden(String message) {
        return error(403, message);
    }
} 