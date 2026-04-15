package com.qg.server.service;

import java.util.Map;

/**
 * 令牌刷新服务
 */
public interface TokenRefreshService {
    /**
     * 刷新令牌
     * @param refreshToken 刷新令牌
     * @return 刷新后的令牌
     */
    Map<String, String> refreshTokens(String refreshToken);

    /**
     * 添加令牌到黑名单
     * @param token 令牌
     */
    void addTokenToBlacklist(String token);

    /**
     * 检查令牌是否在黑名单中
     * @param token 令牌
     * @return 是否在黑名单中
     */
    boolean isBlacklisted(String token);
}
