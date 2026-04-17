package com.qg.server.ai.client;

import com.qg.common.constant.MessageConstant;
import com.qg.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

/**
 * 管理员统计AI
 */
@Slf4j
@RequiredArgsConstructor
public class AdminStatisticsAiClient {
    private final ChatClient chatClient;

    /**
     * 管理员统计AI总结
     *
     * @param prompt 提示词
     * @return 总结结果
     */
    public String generateSummary(String prompt) {
        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("管理员统计AI总结失败", e);
            return MessageConstant.AI_GENERATE_FAILED;
        }
    }
}
