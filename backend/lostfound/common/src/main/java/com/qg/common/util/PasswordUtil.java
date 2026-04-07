package com.qg.common.util;


import com.qg.common.constant.MessageConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
/**
 * 密码加密工具类
 * 自动加盐，无法反解密，企业标准方案
 */
@Slf4j
public class PasswordUtil {

    // 直接使用 Spring 内置的 BCrypt 加密器
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * 私有构造器，防止工具类被实例化
     */
    private PasswordUtil() {
    }

    /**
     * BCrypt 加密密码（自动加盐，每次结果不同）
     *
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    public static String encrypt(String rawPassword) {
        log.debug("BCrypt 密码加密开始");
        try {
            String encode = PASSWORD_ENCODER.encode(rawPassword);
            log.debug("BCrypt 密码加密完成");
            return encode;
        } catch (Exception e) {
            log.error("密码加密异常", e);
            throw new RuntimeException(MessageConstant.PASSWORD_ENCRYPT_ERROR, e);
        }
    }

    /**
     * 密码比对（安全方式）
     *
     * @param rawPassword       用户输入的明文密码
     * @param encryptedPassword 数据库存储的加密密码
     * @return 匹配结果
     */
    public static boolean matches(String rawPassword, String encryptedPassword) {
        log.debug("BCrypt 密码校验开始");
        try {
            boolean matches = PASSWORD_ENCODER.matches(rawPassword, encryptedPassword);
            log.debug("BCrypt 密码校验结果：{}", matches);
            return matches;
        } catch (Exception e) {
            log.error("密码校验异常", e);
            return false;
        }
    }
}