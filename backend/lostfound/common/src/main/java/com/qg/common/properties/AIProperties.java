package com.qg.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="ai")
@Data
public class AIProperties {
    private String model;
    private int timeoutMs=5000;
    private int dailyLimit=20;
}
