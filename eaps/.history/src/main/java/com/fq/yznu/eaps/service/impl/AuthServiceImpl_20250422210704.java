package com.fq.yznu.eaps.service.impl;

import com.fq.yznu.eaps.common.exception.BusinessException;
import com.fq.yznu.eaps.model.dto.LoginDTO;
import com.fq.yznu.eaps.model.dto.RegisterDTO;
import com.fq.yznu.eaps.model.dto.PasswordDTO;
import com.fq.yznu.eaps.model.vo.UserInfoVO;
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
        String token = jwtTokenUtil.generateAccessToken(user);

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
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername()));
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setRole(registerDTO.getRole());
        user.setStatus(0); // 0表示正常状态
        user.setSecurityQuestion(registerDTO.getSecurityQuestion());
        user.setSecurityAnswer(registerDTO.getSecurityAnswer());

        userMapper.insert(user);
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

    @Override
    public String getSecurityQuestion(String username) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return user.getSecurityQuestion();
    }

    @Override
    public String verifySecurityAnswer(String username, String answer) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!answer.equals(user.getSecurityAnswer())) {
            throw new BusinessException("密保答案不正确");
        }

        // 生成重置密码的token
        return jwtTokenUtil.generateResetToken(user);
    }

    @Override
    public void changePassword(PasswordDTO passwordDTO) {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(PasswordDTO passwordDTO) {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public UserInfoVO getCurrentUserInfo() {
        // 从SecurityContext获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 获取用户基本信息
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 转换为VO
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setId(user.getId());
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setRole(user.getUserType().toString());
        userInfoVO.setEmail(user.getEmail());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setStatus(user.getStatus().toString());

        return userInfoVO;
    }
}