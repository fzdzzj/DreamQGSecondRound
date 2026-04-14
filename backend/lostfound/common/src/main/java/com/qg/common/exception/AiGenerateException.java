package com.qg.common.exception;

/**
 * 智能体生成失败异常
 */
public class AiGenerateException extends BaseException {
    public AiGenerateException(String message) {
        super(403, message);
    }

    public AiGenerateException(Integer code, String message) {
        super(code, message);
    }
}
