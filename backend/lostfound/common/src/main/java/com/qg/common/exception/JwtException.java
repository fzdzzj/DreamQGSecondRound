package com.qg.common.exception;

import lombok.Getter;

/**
 * JWT 认证异常
 * 用于：token过期、无效、未登录、签名错误等
 */
@Getter
public class JwtException extends BaseException {

    /**
     * 默认异常码：未授权 / 登录过期
     */
    public static final int DEFAULT_CODE = 401;

    private final Integer code;

    /**
     * 仅传入异常信息，默认使用 401 状态码
     */
    public JwtException(String message) {
        super(message);
        this.code = DEFAULT_CODE;
    }

    /**
     * 自定义状态码 + 异常信息
     */
    public JwtException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 异常信息 + 原始异常
     */
    public JwtException(String message, Throwable cause) {
        super(message, cause);
        this.code = DEFAULT_CODE;
    }

    /**
     * 自定义状态码 + 异常信息 + 原始异常
     */
    public JwtException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}