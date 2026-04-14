package com.qg.common.enums;

/**
 * WebSocket操作枚举类
 */
public enum WsActionEnum {
    CHAT("CHAT", "聊天消息"),
    READ("READ", "已读回执"),
    SYSTEM("SYSTEM", "系统消息");

    private final String code;
    private final String desc;

    WsActionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    WsActionEnum(String code, String desc, boolean dummy) {
        this.code = code;
        this.desc = desc;
    }
}
