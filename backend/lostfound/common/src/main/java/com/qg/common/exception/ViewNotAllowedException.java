package com.qg.common.exception;

public class ViewNotAllowedException extends BaseException {
    public ViewNotAllowedException( String message) {
        super(403, message);
    }
}
