package com.qg.server.ai.repository;

import com.qg.server.ai.AIMessageVO;

import java.time.Instant;
import java.util.List;

public interface ChatHistoryRepository {

    void saveMessage( String type, Long userId,String chatId, String role, String content);

    List<String> getChatIds(String type, Long userId);

    List<String> getAllChatIds(String type);

    List<AIMessageVO> getChatHistory(String chatId, Long userId,String type);

    void deleteChatHistory(String type, String chatId);

    Instant getFirstMessageTime(String type, String chatId);
}
