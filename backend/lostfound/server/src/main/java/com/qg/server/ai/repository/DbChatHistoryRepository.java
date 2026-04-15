package com.qg.server.ai.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.pojo.entity.AiChatHistory;
import com.qg.pojo.vo.MessageVO;
import com.qg.server.ai.AIMessageVO;
import com.qg.server.mapper.ChatHistoryDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库聊天历史存储实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DbChatHistoryRepository implements ChatHistoryRepository {

    private final ChatHistoryDao dao;
    /**
     * 保存聊天消息
     *
     * @param type      消息类型
     * @param userId    用户ID
     * @param chatId    聊天ID
     * @param role      角色
     * @param content   内容
     */
    @Override
    public void saveMessage(String type, Long userId, String chatId, String role, String content) {
        AiChatHistory history = new AiChatHistory();
        history.setChatId(chatId);
        history.setType(type);
        history.setRole(role);
        history.setContent(content);
        history.setUserId(userId);
        dao.insert(history);
    }
    /**
     * 获取聊天ID列表
     *
     * @param type  消息类型
     * @param userId 用户ID
     * @return 聊天ID列表
     */
    @Override
    public List<String> getChatIds(String type, Long userId) {
        List<AiChatHistory> list = dao.selectList(new LambdaQueryWrapper<AiChatHistory>()
                .eq(AiChatHistory::getType, type)
                .eq(AiChatHistory::getUserId, userId)
                .orderByDesc(AiChatHistory::getCreateTime));

        return list.stream()
                .map(AiChatHistory::getChatId)
                .distinct()
                .toList();
    }
    /**
     * 获取所有聊天ID列表
     *
     * @param type  消息类型
     * @return 聊天ID列表
     */
    @Override
    public List<String> getAllChatIds(String type) {
        List<AiChatHistory> list = dao.selectList(new LambdaQueryWrapper<AiChatHistory>()
                .eq(AiChatHistory::getType, type)
                .orderByDesc(AiChatHistory::getCreateTime));

        return list.stream()
                .map(AiChatHistory::getChatId)
                .distinct()
                .toList();
    }
    /**
     * 获取聊天历史
     *
     * @param chatId 聊天ID
     * @param type   聊天类型
     * @return 第一条消息时间
     */
    @Override
    public Instant getFirstMessageTime(String type, String chatId) {
        return dao.getFirstMessageTime(type, chatId);
    }
    /**
     * 获取聊天历史
     *
     * @param chatId 聊天ID
     * @param userId 用户ID
     * @return 聊天历史列表
     */
    @Override
    public List<MessageVO> list(String chatId, Long userId) {
        return dao.selectByChatId(chatId, userId)
                .stream()
                .map(e -> new MessageVO(e.getRole(), e.getContent()))
                .collect(Collectors.toList());
    }

    /**
     * 删除聊天历史
     *
     * @param type 消息类型
     * @param chatId 聊天ID
     */
    @Override
    public void deleteChatHistory(String type, String chatId) {
        dao.deleteChatHistory(type, chatId);
    }
}
