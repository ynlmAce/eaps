package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.dto.LoginDTO;

import java.util.Map;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return token和用户信息
     */
    Map<String, Object> login(LoginDTO loginDTO);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     */
    void register(Map<String, Object> registerDTO);

    /**
     * 找回密码
     *
     * @param username 用户名
     * @param email    邮箱
     */
    void forgotPassword(String username, String email);

    /**
     * 重置密码
     *
     * @param token    重置令牌
     * @param password 新密码
     */
    void resetPassword(String token, String password);
} 