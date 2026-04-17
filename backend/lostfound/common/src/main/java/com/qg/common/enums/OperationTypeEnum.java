package com.qg.common.enums;

/**
 * 操作类型枚举类
 */
public enum OperationTypeEnum {
    CLAIM("1", "认领"),
    EDIT_POST("2", "编辑帖子"),
    POST("3", "发布帖子"),
    ADMIN("4", "管理员操作");
    private final String code;
    private final String desc;

    OperationTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDesc(String code) {
        for (OperationTypeEnum value : OperationTypeEnum.values()) {
            if (value.code.equals(code)) {
                return value.desc;
            }
        }
        return "未知";
    }
}
