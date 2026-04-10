package com.qg.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.ai.openai")
public class AIProperties {
    private String apiKey;       // 对应 yml: spring.ai.openai.api-key
    private String model;        // 对应 yml: spring.ai.openai.chat.options.model
    private int timeoutMs = 5000;
    private int dailyLimit = 20;
}
