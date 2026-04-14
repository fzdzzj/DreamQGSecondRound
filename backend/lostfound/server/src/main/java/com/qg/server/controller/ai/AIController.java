package com.qg.server.controller.ai;

import com.qg.common.constant.AiInputOutput;
import com.qg.common.context.BaseContext;
import com.qg.common.properties.AIProperties;
import com.qg.common.result.Result;
import com.qg.common.util.SensitiveWordFilterUtil;
import com.qg.server.ai.repository.ChatHistoryRepository;
import com.qg.server.ai.util.AiUtils;
import com.qg.server.service.AiAsyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    private final AIProperties aiProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AiAsyncService aiAsyncService;
    private final ChatClient answerChatClient;
    private final ChatHistoryRepository chatHistoryRepository;
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
    @RequestMapping(value = "/ask", produces = "text/html;charset=utf-8")
    public Flux<String> ask(String prompt, String chatId) {
        // 检查用户是否超过每日请求限制
        AiUtils.checkUserLimit(BaseContext.getCurrentId(), redisTemplate, aiProperties.getDailyLimit());
        Long userId = BaseContext.getCurrentId();
        prompt = filterUserInput(prompt);
        // 保存用户输入
        chatHistoryRepository.saveMessage("answer", userId, chatId, "user", prompt);

        return answerChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream()
                .content()
                // 处理AI回复
                .doOnNext(reply -> {
                    // 过滤AI回复
                    reply = filterAIOutput(reply);
                    // 保存AI回复
                    chatHistoryRepository.saveMessage("answer", userId, chatId, "assistant", reply);
                });
    }

    /**
     * 过滤用户输入
     * 用于对用户输入进行HTML转义和长度限制
     *
     * @param text 用户输入文本
     * @return 过滤后的用户输入文本
     */
    private String filterUserInput(String text) {
        if (text == null) return "";
        // 移除换行符和首尾空格
        text = text.replaceAll("[\\r\\n]+", " ").trim();
        // 限制用户输入长度
        if (text.length() > AiInputOutput.MAX_USER_PROMPT) {
            text = text.substring(0, AiInputOutput.MAX_USER_PROMPT);
        }
        return text;
    }

    /**
     * 过滤AI回复
     * 用于对AI回复进行敏感词过滤和HTML转义
     *
     * @param text AI回复文本
     * @return 过滤后的AI回复文本
     */
    private String filterAIOutput(String text) {
        if (text == null) return "";
        // 限制AI回复长度
        if (text.length() > AiInputOutput.MAX_AI_OUTPUT) text = text.substring(0, AiInputOutput.MAX_AI_OUTPUT);
        // 过滤敏感词
        text = sensitiveWordFilter.filter(text);
        // 转义HTML特殊字符
        return StringEscapeUtils.escapeHtml4(text);
    }
}
