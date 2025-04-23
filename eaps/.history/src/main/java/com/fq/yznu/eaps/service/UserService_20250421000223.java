package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.User;
import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    Map<String, Object> getCurrentUserInfo();

    /**
     * 更新当前用户信息
     *
     * @param user 用户信息
     */
    void updateUserInfo(User user);

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updatePassword(String oldPassword, String newPassword);

    /**
     * 获取用户列表
     *
     * @param queryParams 查询参数
     * @return 用户列表与分页信息
     */
    Map<String, Object> getUserList(Map<String, Object> queryParams);

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    Map<String, Object> getUserDetail(Long id);

    /**
     * 添加用户
     *
     * @param user 用户信息
     */
    void addUser(User user);

    /**
     * 更新用户
     *
     * @param user 用户信息
     */
    void updateUser(User user);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);
} 