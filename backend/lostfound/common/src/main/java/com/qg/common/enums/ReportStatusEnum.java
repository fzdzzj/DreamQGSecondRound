package com.qg.common.enums;

import lombok.Getter;

@Getter
public enum ReportStatusEnum {

    PENDING("1", "待审核"),
    APPROVED("2", "审核通过"),
    REJECTED("3", "审核驳回");

    private final String code;
    private final String desc;

    ReportStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(String code) {
        for (ReportStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item.desc;
            }
        }
        return "未知";
    }
}
