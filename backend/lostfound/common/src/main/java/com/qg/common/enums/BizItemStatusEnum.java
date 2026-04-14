package com.qg.common.enums;

/**
 * 物品状态枚举类
 */
public enum BizItemStatusEnum {

    OPEN("1", "开放中"),
    MATCHED("2", "已匹配"),
    CLOSED("3", "已关闭"),
    REPORTED("4", "已举报"),
    DELETED("5", "已删除");

    private final String code;
    private final String desc;

    BizItemStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(String code) {
        for (BizItemStatusEnum item : values()) {
            if (item.code.equals(code)) {
                return item.desc;
            }
        }
        return "未知";
    }
}
