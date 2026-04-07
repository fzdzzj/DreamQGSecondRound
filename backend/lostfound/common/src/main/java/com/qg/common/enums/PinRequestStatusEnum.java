package com.qg.common.enums;

public enum PinRequestStatusEnum {
    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "审核通过"),
    REJECTED("REJECTED", "审核驳回"),
    CANCELED("CANCELED", "已取消");


    private final String code;
    private final String desc;

    PinRequestStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(String code) {
        for (PinRequestStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item.desc;
            }
        }
        return code;
    }
}
