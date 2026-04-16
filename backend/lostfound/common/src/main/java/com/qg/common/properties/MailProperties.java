package com.qg.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 邮件配置属性类
 */
@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "spring.mail")
public class MailProperties {
    private String host;
    private String username;
    private String password;
    private int port;
    private String defaultEncoding;
}