package com.fq.yznu.eaps.controller;

import com.fq.yznu.eaps.common.ResponseResult;
import com.fq.yznu.eaps.dto.LoginDTO;
import com.fq.yznu.eaps.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
     * 找回密码
     *
     * @param data 找回密码所需数据
     * @return 成功响应
     */
    @PostMapping("/forgot-password")
    public ResponseResult<Void> forgotPassword(@RequestBody Map<String, String> data) {
        authService.forgotPassword(data.get("username"), data.get("email"));
        return ResponseResult.success();
    }

    /**
     * 重置密码
     *
     * @param data 重置密码所需数据
     * @return 成功响应
     */
    @PostMapping("/reset-password")
    public ResponseResult<Void> resetPassword(@RequestBody Map<String, String> data) {
        authService.resetPassword(data.get("token"), data.get("password"));
        return ResponseResult.success();
    }
} 