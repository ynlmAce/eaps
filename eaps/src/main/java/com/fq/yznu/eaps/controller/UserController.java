package com.fq.yznu.eaps.controller;

import com.fq.yznu.eaps.common.ResponseResult;
import com.fq.yznu.eaps.entity.User;
import com.fq.yznu.eaps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public ResponseResult<Map<String, Object>> getCurrentUserInfo() {
        Map<String, Object> userInfo = userService.getCurrentUserInfo();
        return ResponseResult.success(userInfo);
    }

    /**
     * 更新当前用户信息
     *
     * @param user 用户信息
     * @return 成功响应
     */
    @PutMapping("/update")
    public ResponseResult<Void> updateUserInfo(@RequestBody User user) {
        userService.updateUserInfo(user);
        return ResponseResult.success();
    }

    /**
     * 修改密码
     *
     * @param passwordInfo 密码信息
     * @return 成功响应
     */
    @PutMapping("/password")
    public ResponseResult<Void> updatePassword(@RequestBody Map<String, String> passwordInfo) {
        userService.updatePassword(
                passwordInfo.get("oldPassword"),
                passwordInfo.get("newPassword")
        );
        return ResponseResult.success();
    }

    /**
     * 获取用户列表（管理员权限）
     *
     * @param query 查询参数
     * @return 用户列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Map<String, Object>> getUserList(@RequestParam Map<String, Object> query) {
        Map<String, Object> result = userService.getUserList(query);
        return ResponseResult.success(result);
    }

    /**
     * 获取用户详情（管理员权限）
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Map<String, Object>> getUserDetail(@PathVariable Long id) {
        Map<String, Object> userDetail = userService.getUserDetail(id);
        return ResponseResult.success(userDetail);
    }

    /**
     * 添加用户（管理员权限）
     *
     * @param user 用户信息
     * @return 成功响应
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Void> addUser(@RequestBody User user) {
        userService.addUser(user);
        return ResponseResult.success();
    }

    /**
     * 更新用户（管理员权限）
     *
     * @param user 用户信息
     * @return 成功响应
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.updateUser(user);
        return ResponseResult.success();
    }

    /**
     * 删除用户（管理员权限）
     *
     * @param id 用户ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseResult<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseResult.success();
    }
} 