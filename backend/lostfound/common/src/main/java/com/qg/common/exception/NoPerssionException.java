package com.qg.common.exception;

/**
 * 无权限异常
 */
public class NoPerssionException extends BaseException {
    public NoPerssionException(String message) {
        super(403, message);
    }
}
