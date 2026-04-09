package com.qg.common.exception;

public class AiGenerateException extends BaseException {
    public AiGenerateException(String message) {
        super(403,message);
    }
    public AiGenerateException(Integer code, String message) {
        super(code,message);
    }
}
