package com.qg.common.enums;

public enum BizItemAiResultStatusEnum {
    PENDING("1", "待处理"),
    SUCCESS("2", "处理成功"),
    FAILURE("3", "处理失败");
    private String code;
    private String desc;
    BizItemAiResultStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public String getDesc() {
        for (BizItemAiResultStatusEnum value : values()){
            if (value.code.equals(code)){
                return value.desc;
            }
        }
        return "未知";
    }
}
