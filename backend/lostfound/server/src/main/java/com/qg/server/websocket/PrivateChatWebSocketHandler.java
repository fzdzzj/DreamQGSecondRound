package com.qg.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.pojo.vo.PrivateMessageRealtimeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class PrivateChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    private static final Map<Long, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            SESSION_MAP.put(userId, session);
            log.info("WebSocket连接成功，userId={}", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            SESSION_MAP.remove(userId);
            log.info("WebSocket连接关闭，userId={}", userId);
        }
    }

    public void sendToUser(Long userId, PrivateMessageRealtimeVO message) {
        WebSocketSession session = SESSION_MAP.get(userId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (Exception e) {
            log.error("WebSocket推送失败，userId={}", userId, e);
        }
    }
}
