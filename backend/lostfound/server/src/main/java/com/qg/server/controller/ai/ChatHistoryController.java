package com.qg.server.controller.ai;

import com.qg.pojo.vo.MessageVO;
import com.qg.server.ai.repository.ChatHistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI聊天记录接口
 * 提供AI聊天记录相关的接口，如获取所有聊天记录的ID、获取聊天历史记录等
 */
@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/ai/history")
@Tag(name = "AI聊天记录接口", description = "AI聊天记录接口")
public class ChatHistoryController {
    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatMemory chatMemory;

    /**
     * 获取所有聊天记录的ID
     *
     * @param type 消息类型
     * @return 所有聊天记录的ID
     */
    @GetMapping("/chatIds")
    @Operation(summary = "获取所有聊天记录的ID")
    public List<String> getChatIds(@RequestParam("type") String type, @RequestParam("userId") Long userId) {
        log.info("获取所有聊天记录的ID，用户ID={},消息类型={}", userId, type);
        List<String> chatIds = chatHistoryRepository.getChatIds(type, userId);
        log.info("从数据库中获取的聊天记录ID，用户ID={},消息类型={},ID数量={}", userId, type, chatIds.size());
        return chatIds;
    }

    /**
     * 获取聊天历史记录
     *
     * @param type   消息类型
     * @param chatId 聊天ID
     * @param userId 用户ID
     * @return 聊天历史记录列表
     */
    @GetMapping("/chatHistory")
    @Operation(summary = "获取聊天历史记录")
    public List<MessageVO> getChatHistory(@RequestParam("type") String type, @RequestParam("chatId") String chatId, @RequestParam("userId") Long userId) {
        // 从数据库中获取聊天记录
        log.info("从数据库中获取聊天记录，chatId: {}", chatId);
        List<Message> messages = chatMemory.get(chatId, Integer.MAX_VALUE);
        // 如果数据库中没有记录，返回空记录
        if (messages == null) {
            log.info("数据库中没有记录，chatId: {}", chatId);
            return List.of();
        }
        log.info("从数据库中获取的聊天记录，chatId: {}, messages大小: {}", chatId, messages.size());
        return messages.stream().map(MessageVO::new).toList();
    }
}
