package com.qg.server.ai.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.pojo.entity.AiChatHistory;
import com.qg.server.ai.AIMessageVO;
import com.qg.server.mapper.ChatHistoryDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbChatHistoryRepository implements ChatHistoryRepository {

    private final ChatHistoryDao dao;

    @Override
    public void saveMessage(String type, Long userId, String chatId, String role, String content) {
        log.info("保存消息，用户ID={},聊天ID={},消息类型={},角色={},内容={}", userId, chatId, type, role, content);
        AiChatHistory history = new AiChatHistory();
        history.setChatId(chatId);
        history.setType(type);
        history.setRole(role);
        history.setContent(content);
        history.setUserId(userId);
        dao.insert(history);
        log.info("保存消息成功，用户ID={},聊天ID={},消息类型={},角色={},内容={}", userId, chatId, type, role, content);
    }

    @Override
    public List<String> getChatIds(String type, Long userId) {
        log.info("获取聊天ID列表，用户ID={},消息类型={}", userId, type);
        // 获取用户的所有聊天ID
        List<AiChatHistory> list=dao.selectList(new LambdaQueryWrapper<AiChatHistory>()
                .eq(AiChatHistory::getType,type)
                .eq(AiChatHistory::getUserId,userId));
        log.info("获取聊天ID列表成功，用户ID={},消息类型={},聊天ID列表={}", userId, type, list);
        List<String> chatIds=list.stream().map(AiChatHistory::getChatId).toList();
        return chatIds;
    }

    @Override
    public List<String> getAllChatIds(String type) {
        log.info("获取所有聊天ID列表，消息类型={}", type);
        // 获取所有聊天ID
        List<AiChatHistory> list=dao.selectList(new LambdaQueryWrapper<AiChatHistory>()
                .eq(AiChatHistory::getType,type));
        log.info("获取所有聊天ID列表成功，消息类型={},聊天ID列表={}", type, list);
        List<String> chatIds=list.stream().map(AiChatHistory::getChatId).toList();
        return chatIds;
    }

    @Override
    public List<AIMessageVO> getChatHistory(String chatId, Long userId, String type) {
        log.info("获取聊天历史，用户ID={},聊天ID={},消息类型={}", userId, chatId, type);
        // 获取聊天历史记录
        List<AiChatHistory> list=dao.selectList(new LambdaQueryWrapper<AiChatHistory>()
                .eq(AiChatHistory::getChatId,chatId)
                .eq(AiChatHistory::getUserId,userId)
                .eq(AiChatHistory::getType,type));
        log.info("获取聊天历史成功，用户ID={},聊天ID={},消息类型={},消息列表={}", userId, chatId, type, list);
        // 转换为AIMessageVO
        List<AIMessageVO> messages=list.stream().map(AIMessageVO::new).toList();
        return messages;
    }
    @Override
    public Instant getFirstMessageTime(String type, String chatId) {
        log.info("获取第一条消息时间，聊天ID={},消息类型={}", chatId, type);
        return dao.getFirstMessageTime(type, chatId);
    }
    @Override
    public void deleteChatHistory(String type, String chatId) {
        log.info("删除聊天历史，聊天ID={},消息类型={}", chatId, type);
        dao.deleteChatHistory(type, chatId);
        log.info("删除聊天历史成功，聊天ID={},消息类型={}", chatId, type);
    }
}
