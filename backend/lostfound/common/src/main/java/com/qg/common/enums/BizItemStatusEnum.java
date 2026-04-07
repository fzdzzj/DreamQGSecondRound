package com.qg.common.enums;

import lombok.Getter;

@Getter
public enum BizItemStatusEnum {

    OPEN("OPEN", "开放中"),
    MATCHED("MATCHED", "已匹配"),
    CLOSED("CLOSED", "已关闭"),
    REPORTED("REPORTED", "已举报"),
    DELETED("DELETED", "已删除");

    private final String code;
    private final String desc;

    BizItemStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(String code) {
        for (BizItemStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item.desc;
            }
        }
        return code;
    }
}
