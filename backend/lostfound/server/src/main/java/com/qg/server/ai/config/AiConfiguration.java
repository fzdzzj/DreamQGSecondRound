package com.qg.server.ai.config;

import com.qg.common.constant.AiPromptConstant;
import com.qg.common.properties.AIProperties;
import com.qg.server.ai.client.AdminStatisticsAiClient;
import com.qg.server.ai.client.DescriptionClient;
import com.qg.server.ai.client.ImageDescriptionClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class AiConfiguration {

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public ChatClient descriptionChatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(AiPromptConstant.DEFAULT_DESCRIPTION_TEMPLATE)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Bean
    public ChatClient adminStatisticsChatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(AiPromptConstant.ADMIN_STATISTICS_SYSTEM_PROMPT)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Bean
    public DescriptionClient descriptionClient(ChatClient descriptionChatClient,
                                               RedisTemplate<String, Object> redisTemplate,
                                               AIProperties aiProperties) {
        return new DescriptionClient(descriptionChatClient, redisTemplate, aiProperties);
    }

    @Bean
    public ImageDescriptionClient imageDescriptionClient(AIProperties aiProperties,
                                                         RedisTemplate<String, Object> redisTemplate) {
        return new ImageDescriptionClient(aiProperties, redisTemplate);
    }

    @Bean
    public AdminStatisticsAiClient adminStatisticsAiClient(ChatClient adminStatisticsChatClient) {
        return new AdminStatisticsAiClient(adminStatisticsChatClient);
    }
}
