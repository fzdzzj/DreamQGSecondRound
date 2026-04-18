package com.qg.server.task;

import com.qg.server.ai.memory.TemporaryChatMemory;
import com.qg.server.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * 清理聊天记录任务
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CleanChatMemoryTask {

    private final ChatHistoryRepository chatHistoryRepository;
    private final TemporaryChatMemory temporaryChatMemory;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void cleanExpiredSessions() {
        long ttlSeconds = 4 * 3600;

        List<String> chatIds = chatHistoryRepository.getAllChatIds("answer");

        Instant now = Instant.now();

        for (String chatId : chatIds) {
            Instant firstMsgTime = chatHistoryRepository.getFirstMessageTime("answer", chatId);
            if (firstMsgTime != null && now.getEpochSecond() - firstMsgTime.getEpochSecond() > ttlSeconds) {
                chatHistoryRepository.deleteChatHistory("answer", chatId);
                log.info("删除过期聊天记录，聊天ID={}", chatId);
            }
        }
        List<String> chatIds2 = chatHistoryRepository.getAllChatIds("assistant");
        for (String chatId : chatIds2) {
            Instant firstMsgTime = chatHistoryRepository.getFirstMessageTime("assistant", chatId);
            if (firstMsgTime != null && now.getEpochSecond() - firstMsgTime.getEpochSecond() > ttlSeconds) {
                chatHistoryRepository.deleteChatHistory("assistant", chatId);
                log.info("删除过期聊天记录，聊天ID={}", chatId);
            }
        }
    }

    // 每10分钟清理一次，过期时间 1 天（86400秒）
    @Scheduled(cron = "0 */10 * * * ?")
    public void clean() {
        temporaryChatMemory.cleanExpired(4 * 3600);
    }
}
