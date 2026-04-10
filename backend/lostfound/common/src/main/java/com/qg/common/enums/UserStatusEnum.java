package com.qg.common.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {

    ENABLE(1, "启用"),
    DISABLE(0, "禁用");

    private final Integer code;
    private final String desc;

    UserStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        for (UserStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item.desc;
            }
        }
        return "未知";
    }
}
