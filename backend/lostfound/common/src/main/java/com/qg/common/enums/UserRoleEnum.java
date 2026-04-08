package com.qg.common.enums;

import lombok.Getter;

@Getter
public enum UserRoleEnum {

    STUDENT("STUDENT", "学生"),
    ADMIN("ADMIN", "管理员"),
    SYSTEM("SYSTEM", "系统");

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
        return code;
    }
}
