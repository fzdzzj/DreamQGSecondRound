package com.qg.server.controller.ai;

import com.qg.common.context.BaseContext;
import com.qg.common.result.Result;
import com.qg.pojo.vo.MessageVO;
import com.qg.server.ai.repository.ChatHistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 获取所有聊天记录的ID
     *
     * @param type 消息类型
     * @return 所有聊天记录的ID
     */
    @GetMapping("/chatIds")
    @Operation(summary = "获取所有聊天记录的ID")
    public Result<List<String>> getChatIds(@RequestParam("type") String type) {
        Long userId = BaseContext.getCurrentId();
        log.info("获取所有聊天记录的ID，消息类型={}", type);
        List<String> chatIds = chatHistoryRepository.getChatIds(type, userId);
        log.info("从数据库中获取的聊天记录ID，用户ID={},消息类型={},ID数量={}", userId, type, chatIds.size());
        return Result.success(chatIds);
    }

    /**
     * 获取聊天历史记录
     *
     * @param chatId 聊天ID
     */
    @GetMapping("/{chatId}")
    @Operation(summary = "获取聊天历史记录")
    public Result<List<MessageVO>> getChatHistory(@PathVariable("chatId") String chatId) {
        // 从数据库中获取聊天记录
        log.info("从数据库中获取聊天记录，chatId: {}", chatId);
        Long userId = BaseContext.getCurrentId();
        List<MessageVO> messages = chatHistoryRepository.list(chatId, userId);
        // 如果数据库中没有记录，返回空记录
        if (messages == null) {
            log.info("数据库中没有记录，chatId: {}", chatId);
            return Result.success(List.of());
        }
        log.info("从数据库中获取的聊天记录，chatId: {}, messages大小: {}", chatId, messages.size());
        return Result.success(messages);
    }
}
