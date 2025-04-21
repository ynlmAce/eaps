package com.fq.yznu.eaps.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 安全工具类
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
                authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            // 这里假设用户名就是用户ID的字符串表示
            try {
                return Long.parseLong(userDetails.getUsername());
            } catch (NumberFormatException e) {
                // 如果用户名不是纯数字，可能需要其他方式获取用户ID
                // 这里简化处理，实际项目中可能需要从用户对象中获取ID
                return null;
            }
        }
        return null;
    }
    
    /**
     * 获取当前登录用户名
     *
     * @return 用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            }
            return String.valueOf(principal);
        }
        return null;
    }
    
    /**
     * 判断当前用户是否已认证
     *
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
} 