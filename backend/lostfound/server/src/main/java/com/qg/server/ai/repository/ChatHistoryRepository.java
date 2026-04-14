package com.qg.server.ai.repository;

import com.qg.server.ai.AIMessageVO;

import java.time.Instant;
import java.util.List;

public interface ChatHistoryRepository {
    /**
     * 保存消息到数据库
     * @param type 消息类型
     * @param userId 用户ID
     * @param chatId 聊天ID
     * @param role 角色
     * @param content 内容
     */
    void saveMessage( String type, Long userId,String chatId, String role, String content);
    /**
     * 获取聊天ID列表
     * @param type 消息类型
     * @param userId 用户ID
     * @return 聊天ID列表
     */
    List<String> getChatIds(String type, Long userId);

    /**
     * 获取所有聊天ID列表
     * @param type 消息类型
     * @return 所有聊天ID列表
     */
    List<String> getAllChatIds(String type);

    /**
     * 获取聊天历史记录
     * @param chatId 聊天ID
     * @param userId 用户ID
     * @param type 消息类型
     * @return 聊天历史记录列表
     */
    List<AIMessageVO> getChatHistory(String chatId, Long userId,String type);

    /**
     * 删除聊天历史记录
     * @param type 消息类型
     * @param chatId 聊天ID
     */
    void deleteChatHistory(String type, String chatId);

    /**
     * 获取第一条消息时间
     * @param type 消息类型
     * @param chatId 聊天ID
     * @return 第一条消息时间
     */
    Instant getFirstMessageTime(String type, String chatId);
}
