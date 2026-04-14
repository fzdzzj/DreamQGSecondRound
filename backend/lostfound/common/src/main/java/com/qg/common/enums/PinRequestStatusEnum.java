package com.qg.common.enums;

/**
 * 帖子请求状态枚举类
 */
public enum PinRequestStatusEnum {
    PENDING("1", "待审核"),
    APPROVED("2", "审核通过"),
    REJECTED("3", "审核驳回"),
    CANCELED("4", "已取消");


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
        return "未知";
    }
}
