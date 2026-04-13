package com.qg.common.enums;

import lombok.Getter;

@Getter
public enum PrivateMessageTypeEnum {
    TEXT("TEXT", "文本"),
    IMAGE("IMAGE", "图片");

    private final String code;
    private final String desc;

    PrivateMessageTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
