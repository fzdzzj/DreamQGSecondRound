package com.qg.server.config;

import com.qg.server.websocket.PrivateChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final PrivateChatWebSocketHandler privateChatWebSocketHandler;
    private final WebSocketAuthHandshakeInterceptor webSocketAuthHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(privateChatWebSocketHandler, "/ws/private-chat")
                .addInterceptors(webSocketAuthHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
