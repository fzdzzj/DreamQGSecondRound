package com.qg.common.enums;

public enum ReadStatusEnum {
    UNREAD(0, "未读"),
    READ(1, "已读");
    private Integer code;
    private String desc;
    ReadStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public String getDesc() {
        for (ReadStatusEnum value : ReadStatusEnum.values()){
            if (value.code.equals(code)){
                return value.desc;
            }
        }
        return "未知";
    }
}
