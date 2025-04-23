package com.fq.yznu.eaps.controller;

import com.fq.yznu.eaps.common.ResponseResult;
import com.fq.yznu.eaps.dto.LoginDTO;
import com.fq.yznu.eaps.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return token和用户基本信息
     */
    @PostMapping("/login")
    public ResponseResult<Map<String, Object>> login(@RequestBody @Valid LoginDTO loginDTO) {
        Map<String, Object> data = authService.login(loginDTO);
        return ResponseResult.success(data);
    }

    /**
     * 用户登出
     *
     * @return 成功响应
     */
    @PostMapping("/logout")
    public ResponseResult<Void> logout() {
        authService.logout();
        return ResponseResult.success();
    }

    /**
     * 注册用户
     *
     * @param registerDTO 注册信息
     * @return 成功响应
     */
    @PostMapping("/register")
    public ResponseResult<Void> register(@RequestBody @Valid Map<String, Object> registerDTO) {
        authService.register(registerDTO);
        return ResponseResult.success();
    }

    /**
     * 获取密保问题
     *
     * @param username 用户名
     * @return 密保问题
     */
    @GetMapping("/security-question/{username}")
    public ResponseResult<String> getSecurityQuestion(@PathVariable String username) {
        String question = authService.getSecurityQuestion(username);
        return ResponseResult.success(question);
    }

    /**
     * 验证密保答案
     *
     * @param data 验证信息
     * @return 重置令牌
     */
    @PostMapping("/verify-security-answer")
    public ResponseResult<String> verifySecurityAnswer(@RequestBody Map<String, String> data) {
        String token = authService.verifySecurityAnswer(data.get("username"), data.get("answer"));
        return ResponseResult.success(token);
    }

    /**
     * 重置密码
     *
     * @param data 重置密码信息
     * @return 成功响应
     */
    @PostMapping("/reset-password")
    public ResponseResult<Void> resetPassword(@RequestBody Map<String, String> data) {
        authService.resetPassword(data.get("token"), data.get("password"));
        return ResponseResult.success();
    }
}