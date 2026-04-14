package com.qg.server.config;

import com.qg.server.websocket.PrivateChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
/**
 * WebSocket 配置类
 * 用于配置 WebSocket 连接和处理
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {
    /**
     * 私有聊天 WebSocket 处理器
     * 用于处理私有聊天消息
     */
    private final PrivateChatWebSocketHandler privateChatWebSocketHandler;
    /**
     * WebSocket 认证握手拦截器
     * 用于验证 WebSocket 连接请求中的 JWT 令牌
     */
    private final WebSocketAuthHandshakeInterceptor webSocketAuthHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(privateChatWebSocketHandler, "/ws/private-chat")
                .addInterceptors(webSocketAuthHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
