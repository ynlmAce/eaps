package com.fq.yznu.eaps.common;

/**
 * 通用响应结果
 *
 * @param <T> 数据类型
 */
public class ResponseResult<T> {
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 成功返回结果
     */
    public static <T> ResponseResult<T> success() {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     */
    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 失败返回结果
     *
     * @param message 提示信息
     */
    public static <T> ResponseResult<T> error(String message) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败返回结果
     *
     * @param code    状态码
     * @param message 提示信息
     */
    public static <T> ResponseResult<T> error(Integer code, String message) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 参数验证失败返回结果
     *
     * @param message 提示信息
     */
    public static <T> ResponseResult<T> validateFailed(String message) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(400);
        result.setMessage(message);
        return result;
    }

    /**
     * 未登录返回结果
     */
    public static <T> ResponseResult<T> unauthorized(String message) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(401);
        result.setMessage(message);
        return result;
    }

    /**
     * 未授权返回结果
     */
    public static <T> ResponseResult<T> forbidden(String message) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(403);
        result.setMessage(message);
        return result;
    }
}