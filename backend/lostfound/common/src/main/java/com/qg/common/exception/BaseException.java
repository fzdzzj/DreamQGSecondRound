package com.qg.common.exception;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BaseException extends RuntimeException {

    /**
     * 通用默认错误码
     */
    public static final int DEFAULT_CODE = 500;

    /**
     * 错误码
     */
    private final Integer code;
    public BaseException(Integer code) {
        super();
        this.code = code;
    }


    /**
     * 只有错误信息（默认500）
     */
    public BaseException(String message) {
        super(message);
        this.code = DEFAULT_CODE;
    }

    /**
     * 错误码 + 错误信息
     */
    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 错误信息 + 原始异常（默认500）
     */
    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.code = DEFAULT_CODE;
    }

    /**
     * 错误码 + 错误信息 + 原始异常
     */
    public BaseException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }


}
