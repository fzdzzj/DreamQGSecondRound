package com.qg.server.controller.ai;

import com.qg.common.constant.AiInputOutput;
import com.qg.common.context.BaseContext;
import com.qg.common.properties.AIProperties;
import com.qg.common.result.Result;
import com.qg.common.util.SensitiveWordFilterUtil;
import com.qg.server.ai.repository.ChatHistoryRepository;
import com.qg.server.ai.util.AiUtils;
import com.qg.server.service.AiAsyncService;
import com.qg.server.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

/**
 * AI接口
 * 提供AI相关功能，如物品描述重新生成、AI问答等
 */
@RestController
@RequestMapping("/ai")
@Tag(name = "AI接口", description = "AI接口")
@Slf4j
@RequiredArgsConstructor
public class AIController {
    private final AiAsyncService aiAsyncService;
    private final AiChatService aiChatService;
    private final SensitiveWordFilterUtil sensitiveWordFilter;

    /**
     * 重新生成物品AI描述
     *
     * @param itemId 物品ID
     * @return 重新生成物品AI描述结果
     */
    @PostMapping("/item/{itemId}/regenerate")
    @Operation(summary = "重新生成物品AI描述")
    public Result<Void> regenerateItemDescription(@PathVariable Long itemId) {
        Long userId = BaseContext.getCurrentId();
        log.info("重新生成物品AI描述，用户ID={},物品ID={}", userId, itemId);
        aiAsyncService.regenerateItemDescription(itemId, userId);
        log.info("重新生成物品AI描述成功，用户ID={},物品ID={}", userId, itemId);
        return Result.success();
    }

    /**
     * AI问答
     *
     * @param prompt 用户输入
     * @param chatId 会话ID
     * @return AI回复
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI流式对话")
    public Flux<String> chat(@RequestParam("prompt") String prompt,
                             @RequestParam("chatId") String chatId) {
        Long userId = BaseContext.getCurrentId();
        return aiChatService.streamAnswer(prompt, chatId, userId);
    }

}
