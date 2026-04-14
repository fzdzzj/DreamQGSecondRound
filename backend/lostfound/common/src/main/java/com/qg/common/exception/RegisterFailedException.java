package com.qg.common.exception;

/**
 * 注册失败异常
 */
public class RegisterFailedException extends BaseException {
    public RegisterFailedException(String msg) {
        super(401, msg);
    }
}
