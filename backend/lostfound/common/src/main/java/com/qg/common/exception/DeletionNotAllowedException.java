package com.qg.common.exception;

public class DeletionNotAllowedException extends BaseException {

    public DeletionNotAllowedException(String msg) {
        super(403,msg);
    }

}
