package com.fq.yznu.eaps.util;

import com.fq.yznu.eaps.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 */
@Component
public class JwtTokenUtil {

    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_CREATED = "created";
    private static final String CLAIM_KEY_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_RESET = "reset";

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.reset-expiration}")
    private Long resetExpiration;

    private Key key;

    public JwtTokenUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Long userId;
        try {
            Claims claims = getAllClaimsFromToken(token);
            userId = Long.parseLong(claims.getId());
        } catch (Exception e) {
            userId = null;
        }
        return userId;
    }

    /**
     * 验证token是否有效
     */
    public boolean validateToken(String token, User user) {
        String username = getUsernameFromToken(token);
        return username != null && username.equals(user.getUsername())
                && !isTokenExpired(token)
                && TOKEN_TYPE_ACCESS.equals(getTokenTypeFromToken(token));
    }

    /**
     * 从token中获取token类型
     */
    private String getTokenTypeFromToken(String token) {
        String tokenType;
        try {
            Claims claims = getAllClaimsFromToken(token);
            tokenType = claims.get(CLAIM_KEY_TYPE, String.class);
        } catch (Exception e) {
            tokenType = null;
        }
        return tokenType;
    }

    /**
     * 判断token是否已经失效
     */
    private boolean isTokenExpired(String token) {
        Date expiredDate = getExpirationDateFromToken(token);
        return expiredDate != null && expiredDate.before(new Date());
    }

    /**
     * 从token中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 生成访问token
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成重置密码token
     */
    public String generateResetToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 300000)) // 5分钟有效期
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从token中获取JWT中的负载
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证重置密码token是否有效
     */
    public boolean validateResetToken(String token) {
        try {
            if (isTokenExpired(token)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从重置密码token中获取用户ID
     */
    public Long getUserIdFromResetToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            if (TOKEN_TYPE_RESET.equals(claims.get(CLAIM_KEY_TYPE))) {
                return Long.parseLong(claims.getId());
            }
        } catch (Exception e) {
            log.error("从重置密码token中获取用户ID失败", e);
        }
        return null;
    }

    /**
     * 验证token是否有效（用于过滤器）
     */
    public boolean validateToken(String token) {
        try {
            if (isTokenExpired(token)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
}