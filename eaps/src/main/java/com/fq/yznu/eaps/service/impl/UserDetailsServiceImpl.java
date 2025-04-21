package com.fq.yznu.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fq.yznu.eaps.entity.User;
import com.fq.yznu.eaps.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 设置角色
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        String role;
        switch (user.getUserType()) {
            case 0:
                role = "ROLE_STUDENT";
                break;
            case 1:
                role = "ROLE_TEACHER";
                break;
            case 2:
                role = "ROLE_ADMIN";
                break;
            case 3:
                role = "ROLE_ENTERPRISE";
                break;
            default:
                role = "ROLE_USER";
        }
        authorities.add(new SimpleGrantedAuthority(role));

        // 返回UserDetails对象
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getStatus() != 1) // status为1表示启用，其他值表示禁用
                .build();
    }
}