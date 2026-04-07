package com.qg.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Access Token 签名密钥
     */
    private String signKey;

    /**
     * Access Token 过期时间（1天）
     */
    private Long expire;

    /**
     * Refresh Token 过期时间（7 天）
     */
    private Long refreshExpire;

    /**
     * Refresh Token 签名密钥（可与 Access Token 相同）
     */
    private String refreshSignKey;
}