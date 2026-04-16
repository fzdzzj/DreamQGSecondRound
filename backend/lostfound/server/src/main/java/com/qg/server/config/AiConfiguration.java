package com.qg.server.config;

import com.qg.common.constant.AiPromptConstant;
import com.qg.common.properties.AIProperties;
import com.qg.common.util.SensitiveWordFilterUtil;
import com.qg.server.ai.client.AdminStatisticsAiClient;
import com.qg.server.ai.client.DescriptionClient;
import com.qg.server.ai.client.ImageDescriptionClient;
import com.qg.server.ai.memory.TemporaryChatMemory;
import com.qg.server.ai.tools.ItemTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * AI 配置类
 */
@Configuration
public class AiConfiguration {
    /**
     * 敏感词过滤工具
     */
    @Autowired
    private SensitiveWordFilterUtil sensitiveWordFilterUtil;


        /**
         * 描述客户端
         */
    @Bean
    public ChatClient descriptionChatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(AiPromptConstant.DEFAULT_DESCRIPTION_TEMPLATE)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
    /**
     * 管理员统计客户端
     */
    @Bean
    public ChatClient adminStatisticsChatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(AiPromptConstant.ADMIN_STATISTICS_SYSTEM_PROMPT)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
    /**
     * 回复客户端
     */
    @Bean
    public ChatClient answerChatClient(OpenAiChatModel chatModel, ItemTools itemTools, TemporaryChatMemory temporaryChatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem(AiPromptConstant.ANSWER_SYSTEM_PROMPT)
                .defaultAdvisors(new SimpleLoggerAdvisor(),new MessageChatMemoryAdvisor(temporaryChatMemory))
                .defaultTools(itemTools)
                .defaultOptions(
                        ToolCallingChatOptions.builder()
                                .internalToolExecutionEnabled(true) // 核心配置
                                .build()
                )
                .build();
    }
    /**
     * 描述客户端
     */
    @Bean
    public DescriptionClient descriptionClient(ChatClient descriptionChatClient,
                                               RedisTemplate<String, Object> redisTemplate,
                                               AIProperties aiProperties) {
        return new DescriptionClient(descriptionChatClient, redisTemplate, aiProperties, sensitiveWordFilterUtil);
    }
    /**
     * 图片描述客户端
     */
    @Bean
    public ImageDescriptionClient imageDescriptionClient(AIProperties aiProperties,
                                                         RedisTemplate<String, Object> redisTemplate) {
        return new ImageDescriptionClient(aiProperties, redisTemplate, sensitiveWordFilterUtil);
    }
    /**
     * 管理员统计客户端
     */
    @Bean
    public AdminStatisticsAiClient adminStatisticsAiClient(ChatClient adminStatisticsChatClient) {
        return new AdminStatisticsAiClient(adminStatisticsChatClient);
    }
}
