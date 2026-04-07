package com.qg.common.exception;

public class UserNotLoginException extends BaseException {



    public UserNotLoginException(String msg) {
        super(401,msg);
    }

}
