package com.qg.common.exception;

/**
 * 登录失败异常
 */
public class LoginFailedException extends BaseException {
    public LoginFailedException(String msg) {
        super(401, msg);
    }
}
