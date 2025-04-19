package com.fq.yznu.eaps.service.impl;

import com.fq.yznu.eaps.common.exception.BusinessException;
import com.fq.yznu.eaps.dto.LoginDTO;
import com.fq.yznu.eaps.entity.User;
import com.fq.yznu.eaps.mapper.UserMapper;
import com.fq.yznu.eaps.service.AuthService;
import com.fq.yznu.eaps.util.JwtTokenUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return token和用户信息
     */
    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        // 进行用户认证
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        
        // 认证通过后，更新SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 获取用户信息
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, loginDTO.getUsername()));
        
        if (user.getStatus() != 0) {
            throw new BusinessException("该账号已被禁用");
        }
        
        // 生成token
        String token = jwtTokenUtil.generateToken(user);
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", user);
        
        return result;
    }

    /**
     * 用户登出
     */
    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(Map<String, Object> registerDTO) {
        // 获取基本信息
        String username = (String) registerDTO.get("username");
        String password = (String) registerDTO.get("password");
        String email = (String) registerDTO.get("email");
        String phone = (String) registerDTO.get("phone");
        Integer userType = (Integer) registerDTO.get("userType");
        
        // 检查用户名是否已存在
        User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // 加密密码
        user.setEmail(email);
        user.setPhonenumber(phone);
        user.setUserType(userType);
        user.setStatus(0); // 正常状态
        
        // 保存用户基本信息
        userMapper.insert(user);
        
        // 根据用户类型，创建对应的扩展信息
        if (userType == 0) { // 学生
            // 创建学生信息，具体字段根据registerDTO中的数据填充
            createStudentInfo(user.getId(), registerDTO);
        } else if (userType == 3) { // 企业
            // 创建企业信息，具体字段根据registerDTO中的数据填充
            createEnterpriseInfo(user.getId(), registerDTO);
        } else if (userType == 4) { // 辅导员
            // 创建辅导员信息，具体字段根据registerDTO中的数据填充
            createCounselorInfo(user.getId(), registerDTO);
        }
    }

    /**
     * 找回密码
     *
     * @param username 用户名
     * @param email    邮箱
     */
    @Override
    public void forgotPassword(String username, String email) {
        // 验证用户名和邮箱是否匹配
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getEmail, email));
        
        if (user == null) {
            throw new BusinessException("用户名或邮箱不正确");
        }
        
        // 生成重置密码的token
        String resetToken = jwtTokenUtil.generateResetToken(user);
        
        // 发送重置密码邮件（实际项目中应该调用邮件服务）
        // sendResetPasswordEmail(user.getEmail(), resetToken);
        
        // 这里简化处理，实际应该记录token到数据库或缓存中
    }

    /**
     * 重置密码
     *
     * @param token    重置令牌
     * @param password 新密码
     */
    @Override
    public void resetPassword(String token, String password) {
        // 验证token有效性
        if (!jwtTokenUtil.validateResetToken(token)) {
            throw new BusinessException("密码重置链接已过期");
        }
        
        // 从token中获取用户ID
        Long userId = jwtTokenUtil.getUserIdFromResetToken(token);
        
        // 更新用户密码
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        user.setPassword(passwordEncoder.encode(password));
        userMapper.updateById(user);
    }
    
    /**
     * 创建学生信息
     *
     * @param userId      用户ID
     * @param registerDTO 注册信息
     */
    private void createStudentInfo(Long userId, Map<String, Object> registerDTO) {
        // 具体实现，需要调用学生信息的Service或Mapper
    }
    
    /**
     * 创建企业信息
     *
     * @param userId      用户ID
     * @param registerDTO 注册信息
     */
    private void createEnterpriseInfo(Long userId, Map<String, Object> registerDTO) {
        // 具体实现，需要调用企业信息的Service或Mapper
    }
    
    /**
     * 创建辅导员信息
     *
     * @param userId      用户ID
     * @param registerDTO 注册信息
     */
    private void createCounselorInfo(Long userId, Map<String, Object> registerDTO) {
        // 具体实现，需要调用辅导员信息的Service或Mapper
    }
} 