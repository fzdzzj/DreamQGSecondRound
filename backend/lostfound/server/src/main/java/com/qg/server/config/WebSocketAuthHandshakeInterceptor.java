package com.qg.server.config;

import com.qg.common.constant.UserStatusConstant;
import com.qg.common.util.JwtUtil;
import com.qg.pojo.entity.SysUser;
import com.qg.server.mapper.UserDao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * WebSocket 鉴权拦截器
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDao dao;

    /**
     * 验证 WebSocket 连接请求
     * 用于在 WebSocket 连接前验证请求中的 JWT 令牌
     * 如果令牌有效，将用户 ID 放入 attributes 中
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes
     * @return
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   org.springframework.web.socket.WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String token = null;
        // 从请求头中获取 JWT 令牌
        String auth = request.getHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        }

        if (token == null || token.isBlank()) {
            MultiValueMap<String, String> queryParams =
                    UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
            token = queryParams.getFirst("token");
        }

        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            // 解析 JWT 令牌，获取用户 ID
            Long userId = jwtUtil.getUserIdFromToken(token);

            if (userId == null) {
                return false;
            }
            SysUser user = dao.selectById(userId);
            if(user == null|| UserStatusConstant.DISABLE.equals(user.getStatus())){
                return false;
            }
            // 将用户 ID 放入 attributes 中
            attributes.put("userId", userId);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 握手成功后处理
     * 用于在 WebSocket 连接成功后进行一些操作
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param exception
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               org.springframework.web.socket.WebSocketHandler wsHandler, Exception exception) {
    }
}
