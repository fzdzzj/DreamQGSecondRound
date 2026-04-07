package com.qg.server.service;

import java.util.Map;

public interface TokenRefreshService {

    Map<String, String> refreshTokens(String refreshToken);

    void addTokenToBlacklist(String token);

    boolean isBlacklisted(String token);
}
