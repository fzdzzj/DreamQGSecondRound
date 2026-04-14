package com.qg.common.constant;

/**
 * 正则表达式常量类
 */
public class RegexConstant {


    private RegexConstant() {
    }

    /**
     * 验证码正则：6位数字
     */
    public static final String CODE = "^\\d{6}$";
    /**
     * 用户标识正则：用户名或邮箱
     * 示例：dreamqg、dreamqg@example.com
     */
    public static final String IDENTIFIER = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$|^[A-Za-z0-9_\\-]{4,20}$";
    /**
     * 邮箱正则
     * 示例：dreamqg@example.com
     */
    public static final String EMAIL = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    /**
     * 手机号正则：13800000000
     * 示例：1380000
     */
    public static final String PHONE = "^1[3-9]\\d{9}$";


    /**
     * 密码正则：6-10位字母/数字，无特殊字符
     * 示例：dreamq
     */
    public static final String PASSWORD = "[a-zA-Z0-9]{6,10}";

}
