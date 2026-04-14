package com.qg.common.exception;

/**
 * 描述：用户未登录异常
 */
public class UserNotLoginException extends BaseException {


    public UserNotLoginException(String msg) {
        super(401, msg);
    }

}
