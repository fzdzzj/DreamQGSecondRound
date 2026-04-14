package com.qg.common.exception;

/**
 * 删除数据异常
 */
public class DeletionNotAllowedException extends BaseException {

    public DeletionNotAllowedException(String msg) {
        super(403, msg);
    }

}
