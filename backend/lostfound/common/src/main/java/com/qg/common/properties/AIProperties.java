package com.qg.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 智能体配置属性类
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.ai.openai")
public class AIProperties {
    private String apiKey;       // 对应 yml: spring.ai.openai.api-key
    private String model;        // 对应 yml: spring.ai.openai.chat.options.model
    private int timeoutMs = 90000;// 多少秒 超时时间
    private int dailyLimit = 20;
}
