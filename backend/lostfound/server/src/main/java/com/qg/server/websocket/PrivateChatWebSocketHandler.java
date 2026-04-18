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

@Slf4j
@Component
@RequiredArgsConstructor
public class PrivateChatWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionService webSocketSessionService;

    /**
     * WebSocket连接建立处理
     * @param session
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            log.info("WebSocket连接建立, sessionId={}, attributes={}", session.getId(), session.getAttributes());

            Long userId = resolveUserId(session);
            if (userId == null) {
                log.warn("WebSocket连接建立但未找到userId, sessionId={}", session.getId());
                return;
            }

            webSocketSessionService.addSession(userId, session);
            log.info("WebSocket连接成功, userId={}, sessionId={}", userId, session.getId());
        } catch (Exception e) {
            log.error("WebSocket连接建立后处理失败, sessionId={}", session.getId(), e);
        }
    }

    /**
     * 处理WebSocket文本消息
     * @param session
     * @param message
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            log.info("收到WebSocket文本消息, sessionId={}, payload={}", session.getId(), message.getPayload());

            JSONObject json = JSON.parseObject(message.getPayload());
            String type = json.getString("type");

            if (WsMessageType.PING.equals(type)) {
                log.info("收到WebSocket PING消息, sessionId={}", session.getId());
                send(session, WsMessageEnvelope.of(WsMessageType.PONG, "pong"));
                return;
            }

            log.info("不支持的WebSocket消息类型, sessionId={}, type={}", session.getId(), type);
            send(session, WsMessageEnvelope.of(WsMessageType.ERROR, "不支持的消息类型"));
        } catch (Exception e) {
            log.warn("处理WebSocket消息失败, sessionId={}, payload={}", session.getId(), message.getPayload(), e);
            send(session, WsMessageEnvelope.of(WsMessageType.ERROR, "消息格式错误"));
        }
    }
    /**
     * WebSocket连接关闭处理
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            Long userId = resolveUserId(session);
            if (userId == null) {
                log.warn("WebSocket连接关闭时未找到userId, sessionId={}, code={}",
                        session.getId(), status.getCode());
                return;
            }

            webSocketSessionService.removeSession(userId, session);
            log.info("WebSocket连接关闭, userId={}, sessionId={}, code={}",
                    userId, session.getId(), status.getCode());
        } catch (Exception e) {
            log.error("WebSocket连接关闭后处理失败, sessionId={}, code={}",
                    session.getId(), status.getCode(), e);
        }
    }

    /**
     * 给指定会话发送消息
     * 1. 检查session是否为空
     * 2. 检查session是否已关闭
     * 3. 从session中解析userId
     * 4. 发送消息
     */
    private void send(WebSocketSession session, WsMessageEnvelope envelope) {
        try {
            //1. 检查session是否为空
            if (session == null) {
                log.warn("发送WebSocket消息失败: session为空");
                return;
            }
            //2. 检查session是否已关闭
            if (!session.isOpen()) {
                log.warn("发送WebSocket消息失败: session已关闭, sessionId={}", session.getId());
                return;
            }
            //3. 从session中解析userId
            Long userId = resolveUserId(session);
            String json = JSON.toJSONString(envelope);

            log.info("发送WebSocket消息, userId={}, sessionId={}, type={}, payload={}",
                    userId, session.getId(), envelope.getType(), json);
            //4. 发送消息
            session.sendMessage(new TextMessage(json));

            log.info("发送WebSocket消息成功, userId={}, sessionId={}, type={}",
                    userId, session.getId(), envelope.getType());
        } catch (Exception e) {
            log.error("发送WebSocket消息失败, sessionId={}", session == null ? null : session.getId(), e);
        }
    }

    /**
     * 给指定用户发送消息
     */
    public void sendToUser(Long userId, Object data, String type) {
        WebSocketSession session = webSocketSessionService.getSession(userId);
        //1. 检查用户是否在线
        if (session == null) {
            log.info("用户不在线或WebSocket会话不存在, userId={}", userId);
            return;
        }
        //2. 检查session是否已关闭
        if (!session.isOpen()) {
            log.info("用户WebSocket会话已关闭, userId={}, sessionId={}", userId, session.getId());
            return;
        }

        log.info("准备发送WebSocket消息给指定用户, userId={}, sessionId={}, type={}",
                userId, session.getId(), type);
        //3. 发送消息
        send(session, WsMessageEnvelope.of(type, data));
    }

    /**
     * 安全解析 session 中的 userId
     * 1. 从session中获取userId
     * 2. 检查userId是否为Long类型
     * 3. 检查userId是否为Integer类型
     * 4. 尝试将userId转换为Long类型
     * @return userId
     */
    private Long resolveUserId(WebSocketSession session) {
        //1. 从session中获取userId
        Object rawUserId = session.getAttributes().get("userId");
        if (rawUserId == null) {
            return null;
        }
        //2. 检查userId是否为Long类型

        if (rawUserId instanceof Long) {
            return (Long) rawUserId;
        }
        //3. 检查userId是否为Integer类型
        if (rawUserId instanceof Integer) {
            return ((Integer) rawUserId).longValue();
        }
        //4. 尝试将userId转换为Long类型
        return Long.valueOf(String.valueOf(rawUserId));
    }
}
