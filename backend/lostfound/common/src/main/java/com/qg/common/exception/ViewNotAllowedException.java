package com.qg.common.exception;

/**
 * 访问权限异常
 */
public class ViewNotAllowedException extends BaseException {
    public ViewNotAllowedException(String message) {
        super(403, message);
    }
}
