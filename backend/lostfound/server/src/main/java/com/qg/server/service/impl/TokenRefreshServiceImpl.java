package com.qg.server.service.impl;

import com.qg.common.constant.MessageConstant;
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
 *
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

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    @Override
    public Map<String, String> refreshTokens(String refreshToken) {
        log.info("开始刷新 accessToken");

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BaseException(401, MessageConstant.REFRESH_TOKEN_NOT_EMPTY);
        }

        if (isBlacklisted(refreshToken)) {
            throw new BaseException(401, MessageConstant.REFRESH_TOKEN_INVALID + "，请重新登录");
        }

        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw new BaseException(401, MessageConstant.REFRESH_TOKEN_EXPIRED + "，请重新登录");
        }

        String type = jwtUtil.getTypeFromToken(refreshToken);
        if (!"refresh".equals(type)) {
            throw new BaseException(401, MessageConstant.TOKEN_TYPE_ILLEGAL);
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        String role = jwtUtil.getRoleFromToken(refreshToken);

        if (userId == null || username == null || role == null) {
            throw new BaseException(401, MessageConstant.TOKEN_INVALID);
        }

        Set<String> permissions = permissionService.getPermissionsByRole(role);

        String newAccessToken = jwtUtil.generateAccessToken(userId, username, role, permissions);

        Map<String, String> tokens = new HashMap<>();
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

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
