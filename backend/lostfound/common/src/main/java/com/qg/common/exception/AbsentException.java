package com.qg.common.exception;

import java.io.Serializable;

public class AbsentException extends BaseException{
    public AbsentException(String message) {
        super(400,message);
    }
}
