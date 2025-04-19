package com.fq.yznu.eaps.util;

import com.fq.yznu.eaps.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT令牌工具类
 */
@Component
public class JwtTokenUtil {

    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_CREATED = "created";
    private static final String CLAIM_KEY_USER_ID = "userId";
    private static final String CLAIM_KEY_TYPE = "type";
    
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_RESET = "reset";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.reset-expiration}")
    private Long resetExpiration;

    /**
     * 根据用户信息生成token
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, user.getUsername());
        claims.put(CLAIM_KEY_USER_ID, user.getId());
        claims.put(CLAIM_KEY_CREATED, new Date());
        claims.put(CLAIM_KEY_TYPE, TOKEN_TYPE_ACCESS);
        return generateToken(claims);
    }

    /**
     * 从token中获取用户名
     */
    public String getUserNameFromToken(String token) {
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Long userId;
        try {
            Claims claims = getClaimsFromToken(token);
            userId = Long.parseLong(claims.get(CLAIM_KEY_USER_ID).toString());
        } catch (Exception e) {
            userId = null;
        }
        return userId;
    }

    /**
     * 验证token是否有效
     */
    public boolean validateToken(String token, User user) {
        String username = getUserNameFromToken(token);
        return username.equals(user.getUsername()) && !isTokenExpired(token) 
                && TOKEN_TYPE_ACCESS.equals(getTokenTypeFromToken(token));
    }

    /**
     * 判断token是否已经失效
     */
    private boolean isTokenExpired(String token) {
        Date expiredDate = getExpiredDateFromToken(token);
        return expiredDate.before(new Date());
    }

    /**
     * 从token中获取过期时间
     */
    private Date getExpiredDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 从token中获取token类型
     */
    private String getTokenTypeFromToken(String token) {
        String tokenType;
        try {
            Claims claims = getClaimsFromToken(token);
            tokenType = claims.get(CLAIM_KEY_TYPE).toString();
        } catch (Exception e) {
            tokenType = null;
        }
        return tokenType;
    }

    /**
     * 从token中获取JWT中的负载
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // 解析失败，返回null
        }
        return claims;
    }

    /**
     * 根据负载生成JWT token
     */
    private String generateToken(Map<String, Object> claims) {
        String type = (String) claims.get(CLAIM_KEY_TYPE);
        long expirationTime = TOKEN_TYPE_RESET.equals(type) ? resetExpiration : expiration;
        
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate(expirationTime))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 生成token过期时间
     */
    private Date generateExpirationDate(long expirationTime) {
        return new Date(System.currentTimeMillis() + expirationTime * 1000);
    }
    
    /**
     * 生成重置密码的token
     */
    public String generateResetToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, user.getUsername());
        claims.put(CLAIM_KEY_USER_ID, user.getId());
        claims.put(CLAIM_KEY_CREATED, new Date());
        claims.put(CLAIM_KEY_TYPE, TOKEN_TYPE_RESET);
        return generateToken(claims);
    }
    
    /**
     * 验证重置密码token是否有效
     */
    public boolean validateResetToken(String token) {
        return !isTokenExpired(token) && TOKEN_TYPE_RESET.equals(getTokenTypeFromToken(token));
    }
    
    /**
     * 从重置密码token中获取用户ID
     */
    public Long getUserIdFromResetToken(String token) {
        return getUserIdFromToken(token);
    }
} 