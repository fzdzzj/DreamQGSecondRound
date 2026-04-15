package com.qg.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信机器人配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "risk.wecom")
public class WeComRobotProperties {
    private Boolean enabled = true;
    private String webhook;
}
