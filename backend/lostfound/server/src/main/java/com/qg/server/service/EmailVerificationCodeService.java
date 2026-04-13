package com.qg.server.service;

import com.qg.pojo.entity.EmailVerificationCode;
import com.qg.pojo.entity.EmailVerificationCode;

public interface EmailVerificationCodeService {
    void sendCode(String email, String type);
    boolean verifyCode(String email, String type, String code);
}
