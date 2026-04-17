package com.qg.server.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.qg.server.websocket.model.WsMessageEnvelope;
import com.qg.server.websocket.model.WsMessageType;
import com.qg.server.websocket.service.WebSocketSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 私聊 WebSocket 处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateChatWebSocketHandler extends TextWebSocketHandler {
    /**
     * WebSocket 会话服务
     */

    private final WebSocketSessionService webSocketSessionService;

    /**
     * WebSocket 连接成功处理
     *
     * @param session WebSocket 会话
     */

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            log.info("添加WebSocket会话，userId={}", userId);
            webSocketSessionService.addSession(userId, session);
            log.info("WebSocket连接成功，userId={}", userId);
        }
    }

    /**
     * WebSocket 处理文本消息
     *
     * @param session WebSocket 会话
     * @param message 文本消息
     */

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JSONObject json = JSON.parseObject(message.getPayload());
            String type = json.getString("type");

            if (WsMessageType.PING.equals(type)) {
                log.info("收到WebSocket PING消息");
                send(session, WsMessageEnvelope.of(WsMessageType.PONG, "pong"));
                return;
            }
            log.info("处理WebSocket消息，type={}", type);
            send(session, WsMessageEnvelope.of(WsMessageType.ERROR, "不支持的消息类型"));
        } catch (Exception e) {
            log.warn("处理WebSocket消息失败: {}", message.getPayload(), e);
            send(session, WsMessageEnvelope.of(WsMessageType.ERROR, "消息格式错误"));
        }
    }

    /**
     * WebSocket 连接关闭处理
     *
     * @param session WebSocket 会话
     * @param status  关闭状态
     */

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            webSocketSessionService.removeSession(userId);
            log.info("WebSocket连接关闭，userId={}, code={}", userId, status.getCode());
        }
    }

    /**
     * 发送 WebSocket 消息
     *
     * @param session  WebSocket 会话
     * @param envelope 消息封包
     */

    private void send(WebSocketSession session, WsMessageEnvelope envelope) {
        try {
            if (session.isOpen()) {
                log.info("发送WebSocket消息，userId={}, type={}", session.getAttributes().get("userId"), envelope.getType());
                session.sendMessage(new TextMessage(JSON.toJSONString(envelope)));
            }
        } catch (Exception e) {
            log.error("发送WebSocket消息失败", e);
        }
    }

    /**
     * 发送消息给指定用户
     *
     * @param userId 用户ID
     * @param data   消息数据
     * @param type   消息类型
     */
    public void sendToUser(Long userId, Object data, String type) {
        WebSocketSession session = webSocketSessionService.getSession(userId);
        if (session != null && session.isOpen()) {
            log.info("发送WebSocket消息给userId={}, type={}", userId, type);
            send(session, WsMessageEnvelope.of(type, data));
        }
    }
}