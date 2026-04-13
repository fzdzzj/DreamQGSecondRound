package com.qg.common.constant;
/**
 * 正则表达式常量类
 */
public class RegexConstant {


    public static final String CODE = "^\\d{6}$";

    // 私有构造函数
    private RegexConstant() {
    }

    /**
     * 用户标识正则：用户名或邮箱
     * <p>
     * 示例：dreamqg、dreamqg@example.com
     * </p>
     */
    public static final String IDENTIFIER = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$|^[A-Za-z0-9_\\-]{4,20}$";

    public static final String EMAIL = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public static final String PHONE = "^1[3-9]\\d{9}$";
    /**

    /**
     * 密码正则：6-10位字母/数字，无特殊字符
     */
    public static final String PASSWORD = "[a-zA-Z0-9]{6,10}";

}
