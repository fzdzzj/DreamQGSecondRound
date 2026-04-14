package com.qg.common.constant;

/**
 * 验证码常量类
 */
public class CodeTimeConstant {
    private CodeTimeConstant() {
    }

    /**
     * 验证码过期时间（秒）
     */
    public static final long CODE_EXPIRE_TIME = 120;
    /**
     * 隔多久能发送一次验证码（秒）
     */
    public static final int CODE_INTERVAL = 60;
}
