package com.qg.common.exception;

/**
 * 密码错误异常
 */
public class PasswordErrorException extends BaseException {



    public PasswordErrorException(String msg) {
        super(401,msg);
    }

}
