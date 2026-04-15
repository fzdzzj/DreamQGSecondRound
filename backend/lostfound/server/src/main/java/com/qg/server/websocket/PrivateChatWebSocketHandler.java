package com.qg.server.websocket;

import com.alibaba.fastjson2.JSON;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 私聊 WebSocket 处理器
 */
public class PrivateChatWebSocketHandler extends TextWebSocketHandler {

    /**
     * 存储用户 WebSocketSession
     */
    private static final Map<Long, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    /**
     * 连接成功后处理
     * 用于在 WebSocket 连接成功后进行一些操作
     *
     * @param session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        SESSIONS.put(userId, session);
    }
    /**
     * 处理客户端消息
     *
     * @param session
     * @param message
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 处理客户端消息（心跳）
        if ("ping".equals(message.getPayload())) {
            send(session, "pong");
        }
    }
    /**
     * 连接关闭处理
     * 用于在 WebSocket 连接关闭后进行一些操作
     *
     * @param session
     * @param status
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        SESSIONS.remove(userId);
    }
    /**
     * 发送消息给指定用户
     *
     * @param userId
     * @param data
     */
    public static void sendToUser(Long userId, Object data) {
        WebSocketSession session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            send(session, JSON.toJSONString(data));
        }
    }
    /**
     * 发送消息
     *
     * @param session
     * @param msg
     */
    private static void send(WebSocketSession session, String msg) {
        try {
            session.sendMessage(new TextMessage(msg));
        } catch (Exception ignored) {}
    }
}
