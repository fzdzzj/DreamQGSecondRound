package com.qg.common.exception;

public class UpdateNotAllowedException extends BaseException {

    public UpdateNotAllowedException(String msg) {
        super(403,msg);
    }

}