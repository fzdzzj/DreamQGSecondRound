package com.qg.common.enums;

import lombok.Getter;

@Getter
public enum BizItemTypeEnum {

    LOST("LOST", "丢失物品"),
    FOUND("FOUND", "拾取物品");

    private final String code;
    private final String desc;

    BizItemTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(String code) {
        for (BizItemTypeEnum item : values()) {
            if (item.code.equals(code)) {
                return item.desc;
            }
        }
        return code;
    }
}
