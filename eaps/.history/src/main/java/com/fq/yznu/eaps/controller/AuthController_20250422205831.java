package com.fq.yznu.eaps.controller;

import com.fq.yznu.eaps.common.ApiResponse;
import com.fq.yznu.eaps.service.AuthService;
import com.fq.yznu.eaps.model.dto.LoginDTO;
import com.fq.yznu.eaps.model.dto.RegisterDTO;
import com.fq.yznu.eaps.model.dto.PasswordDTO;
import com.fq.yznu.eaps.model.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return token和用户基本信息
     */
    @PostMapping("/login")
    public ApiResponse<String> login(@Validated @RequestBody LoginDTO loginDTO) {
        String token = authService.login(loginDTO);
        return ApiResponse.success("登录成功", token);
    }

    /**
     * 注册用户
     *
     * @param registerDTO 注册信息
     * @return 成功响应
     */
    @PostMapping("/register")
    public ApiResponse<Void> register(@Validated @RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return ApiResponse.success("注册成功");
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public ApiResponse<UserInfoVO> getUserInfo() {
        UserInfoVO userInfo = authService.getCurrentUserInfo();
        return ApiResponse.success(userInfo);
    }

    /**
     * 修改密码
     *
     * @param passwordDTO 密码信息
     * @return 成功响应
     */
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Validated @RequestBody PasswordDTO passwordDTO) {
        authService.changePassword(passwordDTO);
        return ApiResponse.success("密码修改成功");
    }

    /**
     * 重置密码
     *
     * @param passwordDTO 密码信息
     * @return 成功响应
     */
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Validated @RequestBody PasswordDTO passwordDTO) {
        authService.resetPassword(passwordDTO);
        return ApiResponse.success("密码重置成功");
    }

    /**
     * 用户登出
     *
     * @return 成功响应
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success("退出成功");
    }
}