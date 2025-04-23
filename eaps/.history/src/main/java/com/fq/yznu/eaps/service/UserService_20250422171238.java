package com.fq.yznu.eaps.service;

import com.fq.yznu.eaps.entity.User;
import java.util.Map;
import java.util.Optional;

public interface UserService {
    Map<String, Object> getCurrentUserInfo();

    void updateUserInfo(User user);

    void updatePassword(String oldPassword, String newPassword);

    Map<String, Object> getUserList(Map<String, Object> queryParams);

    Map<String, Object> getUserDetail(Long id);

    void addUser(User user);

    void updateUser(User user);

    void deleteUser(Long id);

    Optional<User> findByUsername(String username);

    User save(User user);

    User update(User user);

    void delete(Long id);

    void changePassword(Long id, String newPassword);
}