package com.qg.server.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * WebSocketSessionServiceImpl
 */
@Slf4j
@Service
public class WebSocketSessionServiceImpl implements WebSocketSessionService {

    /**
     * key: userId
     * value: 当前用户最新的 WebSocketSession
     */
    private final Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    /**
     * 保存用户会话
     */
    public void addSession(Long userId, WebSocketSession session) {
        WebSocketSession oldSession = sessionMap.put(userId, session);

        if (oldSession != null && oldSession != session) {
            log.info("覆盖旧WebSocket会话, userId={}, oldSessionId={}, newSessionId={}, 当前在线数={}",
                    userId, oldSession.getId(), session.getId(), sessionMap.size());
        } else {
            log.info("保存WebSocket会话, userId={}, sessionId={}, 当前在线数={}",
                    userId, session.getId(), sessionMap.size());
        }
    }



    /**
     * 获取用户会话
     */
    public WebSocketSession getSession(Long userId) {
        WebSocketSession session = sessionMap.get(userId);
        log.info("获取WebSocket会话, userId={}, sessionId={}",
                userId, session == null ? null : session.getId());
        return session;
    }

    /**
     * 移除用户会话（带session校验，防止误删新连接）
     */
    public void removeSession(Long userId, WebSocketSession session) {
        WebSocketSession current = sessionMap.get(userId);

        if (current == null) {
            log.info("移除WebSocket会话时未找到记录, userId={}, closingSessionId={}",
                    userId, session.getId());
            return;
        }

        if (current.getId().equals(session.getId())) {
            sessionMap.remove(userId);
            log.info("移除WebSocket会话成功, userId={}, sessionId={}, 当前在线数={}",
                    userId, session.getId(), sessionMap.size());
        } else {
            log.info("忽略旧WebSocket会话关闭, userId={}, closingSessionId={}, currentSessionId={}",
                    userId, session.getId(), current.getId());
        }
    }

    /**
     * 判断用户是否在线
     */
    public boolean isOnline(Long userId) {
        WebSocketSession session = sessionMap.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 当前在线人数
     */
    public int onlineCount() {
        return sessionMap.size();
    }
}
