package com.qg.server.service.impl;

import com.qg.common.constant.AiInputOutput;
import com.qg.common.properties.AIProperties;
import com.qg.common.util.SensitiveWordFilterUtil;
import com.qg.server.ai.repository.ChatHistoryRepository;
import com.qg.common.util.AiUtils;
import com.qg.server.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

/**
 * AI 对话服务实现类
 * 基于 Spring AI 实现 SSE 流式对话、上下文记忆、敏感词过滤、对话日志存储
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatServiceImpl implements AiChatService {

    /**
     * 对话类型：普通问答
     */
    private static final String CHAT_TYPE = "answer";

    // AI 配置属性（限流、模型参数等）
    private final AIProperties aiProperties;
    // Redis 用于用户调用次数限流统计
    private final RedisTemplate<String, Object> redisTemplate;
    // Spring AI 对话客户端（带上下文记忆）
    private final ChatClient answerChatClient;
    // 聊天历史记录仓库
    private final ChatHistoryRepository chatHistoryRepository;
    // 敏感词过滤工具
    private final SensitiveWordFilterUtil sensitiveWordFilter;

    /**
     * AI 流式问答接口（SSE 推送）
     * @param prompt 用户输入内容
     * @param chatId 对话会话 ID（用于上下文记忆）
     * @param userId 用户 ID
     * @return Flux<String> SSE 格式的流式消息
     */
    @Override
    public Flux<String> streamAnswer(String prompt, String chatId, Long userId) {
        // 1. 校验用户每日调用次数是否超限
        AiUtils.checkUserLimit(userId, redisTemplate, aiProperties.getDailyLimit());
        // 2. 用户今日调用次数 +1
        AiUtils.incrementUserAiCount(userId, redisTemplate);

        // 3. 过滤用户输入（去换行、超长截断、去空格）
        String filteredPrompt = filterUserInput(prompt);

        // 4. 保存用户提问到聊天历史
        chatHistoryRepository.saveMessage(CHAT_TYPE, userId, chatId, "user", filteredPrompt);

        // 5. 原子引用存储 AI 完整回复（流式片段拼接）
        AtomicReference<StringBuilder> assistantReplyRef = new AtomicReference<>(new StringBuilder());

        // 6. 调用 Spring AI 进行流式对话
        Flux<String> messageFlux = answerChatClient.prompt()
                .user(filteredPrompt)  // 设置用户问题
                // 传入对话 ID，启用上下文记忆
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream()             // 开启流式返回
                .content()            // 只提取 AI 返回的文本内容
                .map(this::filterAIOutput)  // 过滤 AI 回复（敏感词、长度、转义）
                // 拼接每一段流式内容
                .doOnNext(chunk -> assistantReplyRef.get().append(chunk))
                .map(this::toMessageEvent); // 包装成 SSE message 事件

        // 7. 流式结束后：保存完整 AI 回复，并返回 done 事件
        Mono<String> saveAndDoneMono = Mono.fromCallable(() -> {
            String assistantReply = assistantReplyRef.get().toString();
            // 回复非空则保存到历史记录
            if (!assistantReply.isBlank()) {
                chatHistoryRepository.saveMessage(CHAT_TYPE, userId, chatId, "assistant", assistantReply);
            }
            // 返回 SSE 结束事件
            return toDoneEvent();
        });

        // 8. 拼接：消息流 + 结束流 + 异常兜底
        return messageFlux
                .concatWith(saveAndDoneMono)  // 正常流结束后发送 done
                .onErrorResume(e -> {
                    // 异常时打印日志并返回错误事件
                    log.error("AI流式回复失败, userId={}, chatId={}", userId, chatId, e);
                    return Flux.just(toErrorEvent("AI处理失败，请稍后重试"));
                });
    }

    /**
     * 用户输入过滤：清洗格式、超长截断
     */
    private String filterUserInput(String text) {
        if (text == null) {
            return "";
        }
        // 替换换行/回车为空格，去除首尾空格
        text = text.replaceAll("[\\r\\n]+", " ").trim();
        // 超过最大长度则截断
        if (text.length() > AiInputOutput.MAX_USER_PROMPT) {
            text = text.substring(0, AiInputOutput.MAX_USER_PROMPT);
        }
        return text;
    }

    /**
     * AI 输出过滤：敏感词过滤 + HTML 转义 + 长度限制
     */
    private String filterAIOutput(String text) {
        if (text == null) {
            return "";
        }
        // 超长截断
        if (text.length() > AiInputOutput.MAX_AI_OUTPUT) {
            text = text.substring(0, AiInputOutput.MAX_AI_OUTPUT);
        }
        // 敏感词过滤
        text = sensitiveWordFilter.filter(text);
        // HTML 转义，防止前端 XSS
        return StringEscapeUtils.escapeHtml4(text);
    }

    /**
     * 包装 SSE 消息事件：普通消息片段
     */
    private String toMessageEvent(String chunk) {
        return "event: message\n" +
                "data: " + chunk + "\n\n";
    }

    /**
     * 包装 SSE 结束事件：流式传输完成
     */
    private String toDoneEvent() {
        return "event: done\n" +
                "data: [DONE]\n\n";
    }

    /**
     * 包装 SSE 错误事件：异常提示
     */
    private String toErrorEvent(String message) {
        return "event: error\n" +
                "data: " + message + "\n\n";
    }
}