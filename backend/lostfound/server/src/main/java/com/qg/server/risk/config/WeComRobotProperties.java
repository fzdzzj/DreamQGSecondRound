package com.qg.server.risk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "risk.wecom")
public class WeComRobotProperties {
    private Boolean enabled = true;
    private String webhook;
}
