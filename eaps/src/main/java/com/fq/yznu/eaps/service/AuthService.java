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
     * 获取密保问题
     *
     * @param username 用户名
     * @return 密保问题
     */
    String getSecurityQuestion(String username);

    /**
     * 验证密保答案
     *
     * @param username 用户名
     * @param answer   密保答案
     * @return 验证令牌
     */
    String verifySecurityAnswer(String username, String answer);

    /**
     * 重置密码
     *
     * @param token    验证令牌
     * @param password 新密码
     */
    void resetPassword(String token, String password);
}