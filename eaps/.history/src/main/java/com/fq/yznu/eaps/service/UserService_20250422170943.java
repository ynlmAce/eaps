package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.User;
import com.fq.yznu.eaps.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * 用户服务接口
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    public Map<String, Object> getCurrentUserInfo() {
        // Implementation needed
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * 更新当前用户信息
     *
     * @param user 用户信息
     */
    public void updateUserInfo(User user) {
        // Implementation needed
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updatePassword(String oldPassword, String newPassword) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * 获取用户列表
     *
     * @param queryParams 查询参数
     * @return 用户列表与分页信息
     */
    Map<String, Object> getUserList(Map<String, Object> queryParams) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    Map<String, Object> getUserDetail(Long id) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * 添加用户
     *
     * @param user 用户信息
     */
    void addUser(User user) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * 更新用户
     *
     * @param user 用户信息
     */
    void updateUser(User user) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(Long id) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User update(User user) {
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public void changePassword(Long id, String newPassword) {
        userRepository.findById(id).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        });
    }
}