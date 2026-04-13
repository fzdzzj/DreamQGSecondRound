package com.qg.server.ai.client;

import com.qg.common.constant.MessageConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminStatisticsAiClient {
    private final ChatClient chatClient;

    public String generateSummary(String prompt){
        try{
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        }catch(Exception e){
            log.error("管理员统计AI总结失败",e);
            return MessageConstant.AI_GENERATE_FAILED;
        }
    }
}
