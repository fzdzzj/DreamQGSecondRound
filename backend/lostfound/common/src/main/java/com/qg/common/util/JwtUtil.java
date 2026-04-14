package com.qg.common.util;

import com.qg.common.constant.JwtClaimsConstant;
import com.qg.common.constant.TokenConstant;
import com.qg.common.exception.JwtException;
import com.qg.common.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * JWT 工具类
 *
 * 设计说明：
 * 1. 使用双 Token 模式：
 *    - accessToken：访问业务接口，生命周期短，携带权限信息
 *    - refreshToken：仅用于刷新 accessToken，生命周期长，不携带权限信息
 *
 * 2. Token 中统一保存：
 *    - userId：当前登录用户主键
 *    - username：用户名/账号
 *    - role：角色
 *    - type：token 类型（access / refresh）
 *    - permissions：权限集合（仅 accessToken 有）
 *
 * 3. 这样做的好处：
 *    - Filter 解析后可直接恢复 BaseContext
 *    - 避免每次鉴权都查数据库
 *    - 支持 RBAC 权限校验
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    /**
     * JWT 配置（密钥、过期时间）
     */
    private final JwtProperties jwtProperties;

    /**
     * 生成 accessToken
     *
     * @param userId       用户ID
     * @param username     用户名
     * @param role         用户角色
     * @param permissions  权限集合
     * @return accessToken
     */
    public String generateAccessToken(Long userId, String username, String role, Set<String> permissions) {
        return generateToken(userId, username, role, permissions, jwtProperties.getExpire(), "access");
    }

    /**
     * 生成 refreshToken
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     用户角色
     * @return refreshToken
     */
    public String generateRefreshToken(Long userId, String username, String role) {
        return generateToken(userId, username, role, null, jwtProperties.getRefreshExpire(), "refresh");
    }

    /**
     * 统一 Token 生成方法
     */
    private String generateToken(Long userId,
                                 String username,
                                 String role,
                                 Set<String> permissions,
                                 Long expire,
                                 String type) {
        // 构建 Claims
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userId);
        claims.put(JwtClaimsConstant.USERNAME, username);
        claims.put(JwtClaimsConstant.ROLE, role);
        claims.put(JwtClaimsConstant.TYPE, type);

        // 只有 accessToken 才存权限
        if (TokenConstant.ACCESS.equals(type) && permissions != null && !permissions.isEmpty()) {
            claims.put(JwtClaimsConstant.PERMISSIONS, permissions);
        }
        // 构建过期时间
        long now = System.currentTimeMillis();
        Date expiration = new Date(now + expire);

        log.info("生成 {}Token，userId={}, username={}, role={}, expire={}",
                type, userId, username, role, expiration);
        // 生成 Token
        SecretKey secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSignKey().getBytes(StandardCharsets.UTF_8)
        );

        return Jwts.builder()
                .claims(claims)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 Token
     *
     * 说明：
     * - 解析失败直接抛异常，交给上层 Filter 或 Service 统一处理
     */
    public Claims parseToken(String token) {
        try {
            // 验证 Token 签名
            log.info("验证 Token 签名");
            SecretKey secretKey = Keys.hmacShaKeyFor(
                    jwtProperties.getSignKey().getBytes(StandardCharsets.UTF_8)
            );

            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Token 解析失败", e);
            throw new JwtException("Token 解析失败");
        }
    }

    /**
     * 从 Claims 中提取权限集合
     *
     * 说明：
     * - JWT 反序列化后，permissions 可能表现为 List 或 Set
     * - 这里统一转成 Set<String>，方便后续 RBAC 判断
     */
    public Set<String> getPermissionsFromToken(Claims claims) {
        try {
            // 从 Claims 中提取权限集合
            log.info("从 Claims 中提取权限集合");
            Object obj = claims.get(JwtClaimsConstant.PERMISSIONS);
            // 判断 obj 是否为 Iterable 类型，如 List 或 Set
            if (obj instanceof Iterable<?> iterable) {
                Set<String> result = new HashSet<>();
                for (Object item : iterable) {
                    result.add(String.valueOf(item));
                }
                log.info("提取到权限集合：{}", result);
                return result;
            }
            // 如果 obj 不是 Iterable 类型，返回空集合
            log.warn("权限集合格式错误，返回空集合");
            return Collections.emptySet();
        } catch (Exception e) {
            log.error("获取权限失败", e);
            return Collections.emptySet();
        }
    }

    /**
     * 判断 Token 是否过期
     */
    public boolean isTokenExpired(String token) {
        // 解析 Token 并获取 Claims
        log.info("解析 Token 并获取 Claims");
        Claims claims = parseToken(token);
        return claims == null || claims.getExpiration().before(new Date());
    }

    /**
     * 获取 Token 类型
     */
    public String getTypeFromToken(String token) {
        // 解析 Token 并获取 Claims
        Claims claims = parseToken(token);
        return claims == null ? null : claims.get(JwtClaimsConstant.TYPE, String.class);
    }

    /**
     * 获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims == null ? null : claims.get(JwtClaimsConstant.USER_ID, Long.class);
    }

    /**
     * 获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims == null ? null : claims.get(JwtClaimsConstant.USERNAME, String.class);
    }

    /**
     * 获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims == null ? null : claims.get(JwtClaimsConstant.ROLE, String.class);
    }

    /**
     * 获取 Token 剩余有效时间（秒）
     *
     * 用途：
     * - Redis 黑名单 TTL
     */
    public long getExpireFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return -1;
            }
            // 获取 Token 过期时间
            long expireTime = claims.getExpiration().getTime();
            long now = System.currentTimeMillis();
            return (expireTime - now) / 1000;
        } catch (Exception e) {
            log.error("获取 Token 剩余过期时间失败", e);
            return -1;
        }
    }
}
