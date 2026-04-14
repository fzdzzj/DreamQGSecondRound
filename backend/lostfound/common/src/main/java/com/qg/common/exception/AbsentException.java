package com.qg.common.exception;

/**
 * 不存在异常
 */
public class AbsentException extends BaseException {
    public AbsentException(String message) {
        super(400, message);
    }
}
