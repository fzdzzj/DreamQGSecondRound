package com.qg.common.exception;

/**
 * 描述：更新不允许异常
 */
public class UpdateNotAllowedException extends BaseException {

    public UpdateNotAllowedException(String msg) {
        super(403, msg);
    }

}