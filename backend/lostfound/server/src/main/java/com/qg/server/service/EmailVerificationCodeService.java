package com.qg.server.service;

import com.qg.pojo.entity.EmailVerificationCode;
import com.qg.pojo.entity.EmailVerificationCode;

/**
 * 邮箱验证码服务
 */
public interface EmailVerificationCodeService {
    /**
     * 发送验证码
     *
     * @param email 邮箱
     * @param type  验证码类型
     */
    void sendCode(String email, String type);
    /**
     * 验证验证码
     *
     * @param email 邮箱
     * @param type  验证码类型
     * @param code  验证码
     * @return 验证结果
     */
    boolean verifyCode(String email, String type, String code);
}
