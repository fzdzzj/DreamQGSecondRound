package com.qg.common.enums;

/**
 * 私聊消息类型枚举类
 */
public enum PrivateMessageTypeEnum {
    TEXT("1", "文本"),
    IMAGE("2", "图片");

    private final String code;
    private final String desc;

    PrivateMessageTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
