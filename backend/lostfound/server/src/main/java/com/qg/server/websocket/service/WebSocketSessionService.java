package com.qg.server.websocket.service;

import org.springframework.web.socket.WebSocketSession;
/**
 * WebSocketSession服务接口
 */
public interface WebSocketSessionService {
    /**
     * 添加WebSocketSession
     * @param userId 用户ID
     * @param session WebSocketSession
     */
    void addSession(Long userId, WebSocketSession session);
    /**
     * 移除WebSocketSession
     * @param userId 用户ID
     */
    void removeSession(Long userId);
    /**
     * 获取WebSocketSession
     * @param userId 用户ID
     * @return WebSocketSession
     */
    WebSocketSession getSession(Long userId);
    /**
     * 判断用户是否在线
     * @param userId 用户ID
     * @return 是否在线
     */
    boolean isOnline(Long userId);
}
