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

/**
 * 私聊聊天WebSocket处理
 * 每个用户都有一个WebSocket会话，用于实时通信
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PrivateChatWebSocketHandler extends TextWebSocketHandler {
    /**
     *  Jackson
     */
    private final ObjectMapper objectMapper;
    /**
     * 会话映射表
     * 键：用户ID
     * 值：WebSocket会话
         */
    private static final Map<Long, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * 连接成功后，将会话映射到用户ID
     * 1️ 从会话属性中获取用户ID
     * 2️ 将会话映射到用户ID
     * @param session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            SESSION_MAP.put(userId, session);
            log.info("WebSocket连接成功，userId={}", userId);
        }
    }

    /**
     * 连接关闭后，从映射表中移除会话
     * 1️ 从会话属性中获取用户ID
     * 2️ 从映射表中移除会话
     * @param session 关闭的WebSocket会话
     * @param status 关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            SESSION_MAP.remove(userId);
            log.info("WebSocket连接关闭，userId={}", userId);
        }
    }

    /**
     * 发送消息给指定用户
     * 1️ 从映射表中获取会话
     * 2️ 发送消息给会话
     * @param userId 用户ID
     * @param message 消息内容
     */
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
