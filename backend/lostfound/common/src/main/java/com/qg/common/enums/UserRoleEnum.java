package com.qg.common.enums;

import lombok.Getter;

@Getter
public enum UserRoleEnum {

    STUDENT("1", "学生"),
    ADMIN("2", "管理员"),
    SYSTEM("3", "系统");

    private final String code;
    private final String desc;

    UserRoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(String code) {
        for (UserRoleEnum item : values()) {
            if (item.code.equals(code)) {
                return item.desc;
            }
        }
        return "未知";
    }
}
