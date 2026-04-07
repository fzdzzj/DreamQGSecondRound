package com.qg.common.enums;

import lombok.Getter;

@Getter
public enum ReportStatusEnum {

    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "审核通过"),
    REJECTED("REJECTED", "审核驳回");

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
        return code;
    }
}
