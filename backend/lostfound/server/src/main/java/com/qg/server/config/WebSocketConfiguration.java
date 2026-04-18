package com.qg.server.config;

import com.qg.common.util.JwtUtil;
import com.qg.server.mapper.UserDao;
import com.qg.server.websocket.PrivateChatWebSocketHandler;
import com.qg.server.websocket.service.WebSocketSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {
    private final JwtUtil jwtUtil;
    private final UserDao userDao;
    private final WebSocketSessionService webSocketSessionService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(privateChatWebSocketHandler(), "/ws/private-chat")// 注册私聊 WebSocket 处理器
                .addInterceptors(new WebSocketAuthHandshakeInterceptor(jwtUtil, userDao))// 添加鉴权拦截器
                .setAllowedOriginPatterns("*");// 允许所有源访问
    }

    public PrivateChatWebSocketHandler privateChatWebSocketHandler() {
        return new PrivateChatWebSocketHandler(webSocketSessionService);
    }
}
