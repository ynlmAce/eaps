package com.fq.yznu.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fq.yznu.eaps.common.exception.BusinessException;
import com.fq.yznu.eaps.entity.CounselorInfo;
import com.fq.yznu.eaps.entity.EnterpriseInfo;
import com.fq.yznu.eaps.entity.StudentInfo;
import com.fq.yznu.eaps.entity.User;
import com.fq.yznu.eaps.mapper.CounselorInfoMapper;
import com.fq.yznu.eaps.mapper.EnterpriseInfoMapper;
import com.fq.yznu.eaps.mapper.StudentInfoMapper;
import com.fq.yznu.eaps.mapper.UserMapper;
import com.fq.yznu.eaps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StudentInfoMapper studentInfoMapper;

    @Autowired
    private EnterpriseInfoMapper enterpriseInfoMapper;

    @Autowired
    private CounselorInfoMapper counselorInfoMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @Override
    public Map<String, Object> getCurrentUserInfo() {
        // 从SecurityContext中获取当前登录用户名
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询用户基本信息
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (user == null) {
            throw new BusinessException("当前用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);

        // 根据用户类型，查询附加信息
        if (user.getUserType() == 0) { // 学生
            StudentInfo studentInfo = studentInfoMapper.selectOne(
                    new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, user.getId()));
            result.put("studentInfo", studentInfo);
        } else if (user.getUserType() == 3) { // 企业
            EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectOne(
                    new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, user.getId()));
            result.put("enterpriseInfo", enterpriseInfo);
        } else if (user.getUserType() == 4) { // 辅导员
            CounselorInfo counselorInfo = counselorInfoMapper.selectOne(
                    new LambdaQueryWrapper<CounselorInfo>().eq(CounselorInfo::getUserId, user.getId()));
            result.put("counselorInfo", counselorInfo);
        }

        return result;
    }

    /**
     * 更新当前用户信息
     *
     * @param user 用户信息
     */
    @Override
    public void updateUserInfo(User user) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 检查是否是当前用户的信息
        if (!currentUser.getId().equals(user.getId())) {
            throw new BusinessException("无权修改其他用户信息");
        }

        // 设置不允许修改的字段
        user.setUsername(currentUser.getUsername());
        user.setPassword(null); // 不通过此接口修改密码
        user.setUserType(currentUser.getUserType());
        user.setStatus(currentUser.getStatus());

        // 更新用户信息
        userMapper.updateById(user);
    }

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @Override
    public void updatePassword(String oldPassword, String newPassword) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询当前用户
        User currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        // 更新密码
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(currentUser);
    }

    /**
     * 获取用户列表
     *
     * @param queryParams 查询参数
     * @return 用户列表与分页信息
     */
    @Override
    public Map<String, Object> getUserList(Map<String, Object> queryParams) {
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        String username = (String) queryParams.get("username");
        if (StringUtils.hasText(username)) {
            queryWrapper.like(User::getUsername, username);
        }

        String nickname = (String) queryParams.get("nickname");
        if (StringUtils.hasText(nickname)) {
            queryWrapper.like(User::getNickname, nickname);
        }

        Integer userType = (Integer) queryParams.get("userType");
        if (userType != null) {
            queryWrapper.eq(User::getUserType, userType);
        }

        Integer status = (Integer) queryParams.get("status");
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }

        // 查询总数
        long total = userMapper.selectCount(queryWrapper);

        // 分页查询
        long pageNum = Long.parseLong(queryParams.getOrDefault("pageNum", 1).toString());
        long pageSize = Long.parseLong(queryParams.getOrDefault("pageSize", 10).toString());
        Page<User> page = new Page<>(pageNum, pageSize);
        List<User> userList = userMapper.selectPage(page, queryWrapper).getRecords();

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", userList);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);

        return result;
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @Override
    public Map<String, Object> getUserDetail(Long id) {
        // 查询用户基本信息
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);

        // 根据用户类型，查询附加信息
        if (user.getUserType() == 0) { // 学生
            StudentInfo studentInfo = studentInfoMapper.selectOne(
                    new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, user.getId()));
            result.put("studentInfo", studentInfo);
        } else if (user.getUserType() == 3) { // 企业
            EnterpriseInfo enterpriseInfo = enterpriseInfoMapper.selectOne(
                    new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, user.getId()));
            result.put("enterpriseInfo", enterpriseInfo);
        } else if (user.getUserType() == 4) { // 辅导员
            CounselorInfo counselorInfo = counselorInfoMapper.selectOne(
                    new LambdaQueryWrapper<CounselorInfo>().eq(CounselorInfo::getUserId, user.getId()));
            result.put("counselorInfo", counselorInfo);
        }

        return result;
    }

    /**
     * 添加用户
     *
     * @param user 用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(User user) {
        // 检查用户名是否已存在
        User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(0); // 0：正常
        }

        // 保存用户
        userMapper.insert(user);
    }

    /**
     * 更新用户
     *
     * @param user 用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(User user) {
        // 检查用户是否存在
        User existUser = userMapper.selectById(user.getId());
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 如果修改了用户名，检查是否已存在
        if (!existUser.getUsername().equals(user.getUsername())) {
            User usernameUser = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
            if (usernameUser != null) {
                throw new BusinessException("用户名已存在");
            }
        }

        // 如果修改了密码，需要加密
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // 不修改密码
        }

        // 更新用户
        userMapper.updateById(user);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        // 检查用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 删除用户
        userMapper.deleteById(id);

        // 根据用户类型，删除附加信息
        if (user.getUserType() == 0) { // 学生
            studentInfoMapper.delete(
                    new LambdaQueryWrapper<StudentInfo>().eq(StudentInfo::getUserId, id));
        } else if (user.getUserType() == 3) { // 企业
            enterpriseInfoMapper.delete(
                    new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, id));
        } else if (user.getUserType() == 4) { // 辅导员
            counselorInfoMapper.delete(
                    new LambdaQueryWrapper<CounselorInfo>().eq(CounselorInfo::getUserId, id));
        }
    }
} 