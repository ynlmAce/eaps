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
import java.nio.charset.StandardCharsets;

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

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从token中获取用户名
     */
    public String getUserNameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Long userId;
        try {
            Claims claims = getClaimsFromToken(token);
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
        String username = getUserNameFromToken(token);
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
            Claims claims = getClaimsFromToken(token);
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
        Date expiredDate = getExpiredDateFromToken(token);
        return expiredDate != null && expiredDate.before(new Date());
    }

    /**
     * 从token中获取过期时间
     */
    private Date getExpiredDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 生成访问token
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, user.getUsername());
        claims.put(CLAIM_KEY_CREATED, new Date());
        claims.put(CLAIM_KEY_TYPE, TOKEN_TYPE_ACCESS);
        return generateToken(claims);
    }

    /**
     * 生成重置密码token
     */
    public String generateResetToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, user.getUsername());
        claims.put(CLAIM_KEY_CREATED, new Date());
        claims.put(CLAIM_KEY_TYPE, TOKEN_TYPE_RESET);
        return generateToken(claims);
    }

    /**
     * 从token中获取JWT中的负载
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.info("JWT格式验证失败：{}", token);
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
                .claims(claims)
                .expiration(generateExpirationDate(expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成token过期时间
     */
    private Date generateExpirationDate(long expirationTime) {
        return new Date(System.currentTimeMillis() + expirationTime * 1000);
    }

    /**
     * 验证重置密码token是否有效
     */
    public boolean validateResetToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return !isTokenExpired(token) && TOKEN_TYPE_RESET.equals(claims.get(CLAIM_KEY_TYPE));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从重置密码token中获取用户ID
     */
    public Long getUserIdFromResetToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
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
            Claims claims = getClaimsFromToken(token);
            return !isTokenExpired(token) && TOKEN_TYPE_ACCESS.equals(claims.get(CLAIM_KEY_TYPE));
        } catch (Exception e) {
            return false;
        }
    }
}