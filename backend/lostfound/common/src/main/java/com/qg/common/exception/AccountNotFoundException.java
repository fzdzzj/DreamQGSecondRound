package com.qg.common.exception;

/**
 * 账号不存在异常
 */
public class AccountNotFoundException extends BaseException {


    public AccountNotFoundException(String msg) {
        super(404,msg);
    }

}
