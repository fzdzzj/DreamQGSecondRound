package com.qg.common.exception;

public class AccountLockedException extends BaseException {

    public AccountLockedException(String msg) {
        super(403,msg);
    }

}
