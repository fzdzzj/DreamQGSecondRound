package com.qg.server.config;

import com.qg.server.ai.client.DescriptionClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfiguration {
    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }
    @Bean
    public ChatClient serviceChatClient(ChatMemory chatMemory, OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("你是一个热心、可爱的智能助手，请根据物品信息生成失物描述")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

    /**
     * DescriptionClient Bean，用于生成物品描述
     */
    @Bean
    public DescriptionClient descriptionClient(ChatClient serviceChatClient) {
        return new DescriptionClient(serviceChatClient);
    }


}
