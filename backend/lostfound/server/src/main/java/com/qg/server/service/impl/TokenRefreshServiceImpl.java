package com.qg.server.service.impl;

import com.qg.common.constant.MessageConstant;
import com.qg.common.constant.RedisConstant;
import com.qg.common.exception.BaseException;
import com.qg.common.util.JwtUtil;
import com.qg.server.service.PermissionService;
import com.qg.server.service.TokenRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Token 刷新与黑名单服务
 * <p>
 * 核心职责：
 * 1. 用 refreshToken 刷新新的 accessToken
 * 2. 把旧 token 拉入 Redis 黑名单
 * 3. 供过滤器判断 token 是否已失效
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRefreshServiceImpl implements TokenRefreshService {

    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = RedisConstant.TOKEN_BLACKLIST_KEY;

    /**
     * 刷新 accessToken
     *
     * @param refreshToken 刷新 token
     * @return 新的 accessToken
     * <p>
     * 1. 校验 refreshToken 是否为空
     * 2. 校验 refreshToken 是否已被加入黑名单
     * 3. 校验 refreshToken 是否已过期
     * 4. 校验 refreshToken 类型是否为 refresh
     * 5. 校验 refreshToken 中是否缺少必要信息
     * 6. 获取用户权限
     * 7. 返回新的 accessToken
     */
    @Override
    public Map<String, String> refreshTokens(String refreshToken) {
        log.info("开始刷新 accessToken");
        // 1. 校验 refreshToken 是否为空
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("refreshToken 不能为空");
            throw new BaseException(401, MessageConstant.REFRESH_TOKEN_NOT_EMPTY);
        }
        // 2. 校验 refreshToken 是否已被加入黑名单
        if (isBlacklisted(refreshToken)) {
            log.warn("refreshToken 已被加入黑名单");
            throw new BaseException(401, MessageConstant.REFRESH_TOKEN_INVALID + "，请重新登录");
        }
        // 3. 校验 refreshToken 是否已过期
        if (jwtUtil.isTokenExpired(refreshToken)) {
            log.warn("refreshToken 已过期");
            throw new BaseException(401, MessageConstant.REFRESH_TOKEN_EXPIRED + "，请重新登录");
        }
        // 4. 校验 refreshToken 类型是否为 refresh
        String type = jwtUtil.getTypeFromToken(refreshToken);
        if (!"refresh".equals(type)) {
            log.warn("refreshToken 类型错误");
            throw new BaseException(401, MessageConstant.TOKEN_TYPE_ILLEGAL);
        }
        log.info("refreshToken 类型正确");
        // 5. 校验 refreshToken 中是否缺少必要信息
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        String role = jwtUtil.getRoleFromToken(refreshToken);

        if (userId == null || username == null || role == null) {
            log.warn("refreshToken 中缺少必要信息");
            throw new BaseException(401, MessageConstant.TOKEN_INVALID);
        }
        // 6. 获取用户权限
        Set<String> permissions = permissionService.getPermissionsByRole(role);
        log.info("用户{}的权限数量: {}", userId, permissions.size());

        String newAccessToken = jwtUtil.generateAccessToken(userId, username, role, permissions);
        Map<String, String> tokens = new HashMap<>();
        // 7. 返回新的 accessToken
        tokens.put("accessToken", newAccessToken);
        return tokens;
    }

    @Override
    public void addTokenToBlacklist(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        try {
            long expireSeconds = jwtUtil.getExpireFromToken(token);
            if (expireSeconds > 0) {
                String key = BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(key, "1", expireSeconds, TimeUnit.SECONDS);
                log.info("Token 已加入黑名单，剩余有效期={}秒", expireSeconds);
            }
        } catch (Exception e) {
            log.error("Token 加入黑名单异常", e);
        }
    }

    /**
     * 判断 token 是否在黑名单中
     *
     * @param token 待判断的 token
     * @return true 表示在黑名单中，false 表示不在黑名单中
     */
    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return redisTemplate.hasKey(BLACKLIST_PREFIX + token);
    }
}
