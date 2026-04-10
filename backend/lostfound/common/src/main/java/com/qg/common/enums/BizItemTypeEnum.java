package com.qg.common.enums;

import lombok.Getter;

@Getter
public enum BizItemTypeEnum {

    LOST("1", "丢失物品"),
    FOUND("2", "拾取物品");

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
        return "未知";
    }
}
