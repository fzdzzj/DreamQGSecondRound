package com.qg.common.enums;

public enum ReportReasonEnum {

    FAKE_INFO("1", "虚假信息"),
    MALICIOUS("2", "恶意举报"),
    OTHER("3", "其他");
    private final String code;
    private final String desc;
    ReportReasonEnum(String code, String desc) {
                this.code = code;
                this.desc = desc;
    }
    public String getDesc(String  code) {
        for (ReportReasonEnum reason : ReportReasonEnum.values()) {
            if (reason.code.equals(code)) {
                return reason.desc;
            }
        }
        return "未知";
    }

}
