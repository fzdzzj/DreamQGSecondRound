package com.qg.server.websocket.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * WebSocketSession服务实现类
 */
@Service
public class WebSocketSessionServiceImpl implements WebSocketSessionService {
    /**
     * sessionMap
     */
    private final Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();
    /**
     * 添加session
     *
     * @param userId  用户ID
     * @param session WebSocketSession
     */
    @Override
    public void addSession(Long userId, WebSocketSession session) {
        sessionMap.put(userId, session);
    }
    /**
     * 移除session
     *
     * @param userId 用户ID
     */
    @Override
    public void removeSession(Long userId) {
        sessionMap.remove(userId);
    }
    /**
     * 获取session
     *
     * @param userId 用户ID
     * @return WebSocketSession
     */
    @Override
    public WebSocketSession getSession(Long userId) {
        return sessionMap.get(userId);
    }
    /**
     * 判断用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    @Override
    public boolean isOnline(Long userId) {
        WebSocketSession session = sessionMap.get(userId);
        return session != null && session.isOpen();
    }
}
