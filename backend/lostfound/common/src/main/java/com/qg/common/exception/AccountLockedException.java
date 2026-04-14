package com.qg.common.exception;

/**
 * 账号已被锁定异常
 */
public class AccountLockedException extends BaseException {

    public AccountLockedException(String msg) {
        super(403, msg);
    }

}
