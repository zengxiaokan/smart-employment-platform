package com.itzk.SmartEmploymentPlatform.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具 —— 登录认证 Token 生成 / 解析 / 校验
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:please_change_me_in_application_yaml}")
    private String jwtSecret;

    @Value("${jwt.expire:43200000}")
    private long jwtExpire;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /** 生成 Token */
    public String generateToken(Long userId, Byte role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtExpire))
                .signWith(secretKey)
                .compact();
    }

    /** 生成 Token（附带自定义 claims） */
    public String generateToken(Map<String, Object> extraClaims) {
        Date now = new Date();
        var builder = Jwts.builder()
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtExpire));
        if (extraClaims != null && !extraClaims.isEmpty()) {
            extraClaims.forEach(builder::claim);
        }
        return builder.signWith(secretKey).compact();
    }

    /** 解析 Token 并返回 Claims */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从 Token 中获取 userId */
    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /** 从 Token 中获取角色 */
    public Byte getRole(String token) {
        return parseToken(token).get("role", Byte.class);
    }

    /** 校验 Token 是否有效 */
    public boolean validate(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** 校验 Token 是否已过期 */
    public boolean isExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }
}
