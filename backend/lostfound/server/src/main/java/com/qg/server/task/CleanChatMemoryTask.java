package com.qg.server.task;

import com.qg.server.ai.memory.TemporaryChatMemory;
import com.qg.server.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
@Component
@Slf4j
@RequiredArgsConstructor
public class CleanChatMemoryTask {
    private final TemporaryChatMemory chatMemory;
    private final ChatHistoryRepository chatHistoryRepository;

    // 每小时执行一次
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanExpiredSessions() {
        long ttlSeconds = 24 * 3600; // 超过一天

        // 1️ 清理内存
        chatMemory.cleanExpired(ttlSeconds);

        // 2️ 清理数据库
        List<String> chatIds = chatHistoryRepository.getAllChatIds("answer");
        Instant now = Instant.now();
        for (String chatId : chatIds) {
            Instant firstMsgTime = chatHistoryRepository.getFirstMessageTime("answer", chatId);
            if (firstMsgTime != null && now.getEpochSecond() - firstMsgTime.getEpochSecond() > ttlSeconds) {
                chatHistoryRepository.deleteChatHistory("answer", chatId);
                log.info("删除过期聊天记录，聊天ID={}", chatId);
            }
        }
    }
}
