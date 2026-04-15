package com.qg.server.websocket.model;
/**
 * WebSocket 消息类型
 */
public final class WsMessageType {

    private WsMessageType() {
    }

    public static final String PRIVATE_MESSAGE = "PRIVATE_MESSAGE";
    public static final String UNREAD_CHANGED = "UNREAD_CHANGED";
    public static final String PING = "PING";
    public static final String PONG = "PONG";
    public static final String ERROR = "ERROR";
}
