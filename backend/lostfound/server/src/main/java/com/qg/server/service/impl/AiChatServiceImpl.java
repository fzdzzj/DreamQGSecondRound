package com.qg.server.service.impl;

import com.qg.common.constant.AiInputOutput;
import com.qg.common.properties.AIProperties;
import com.qg.common.util.AiUtils;
import com.qg.common.util.SensitiveWordFilterUtil;
import com.qg.server.ai.repository.ChatHistoryRepository;
import com.qg.server.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

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
    // Spring AI 对话客户端（带上下文记忆、带工具）
    private final ChatClient answerChatClient;
    // 聊天历史记录仓库
    private final ChatHistoryRepository chatHistoryRepository;
    // 敏感词过滤工具
    private final SensitiveWordFilterUtil sensitiveWordFilter;

    // Modify to adjust chunk size and delay
    private static final int SSE_CHUNK_SIZE = 50;// 增加块的大小，减少前端渲染次数
    private static final Duration SSE_CHUNK_DELAY = Duration.ofMillis(50); // 增加延迟，减缓推送
    /**
     * 流式回答
     *
     * @param prompt 问题
     * @param chatId 会话ID
     * @param userId 用户ID
     * @return 回答流
     */
    @Override
    public Flux<String> streamAnswer(String prompt, String chatId, Long userId) {
        return Flux.defer(() -> {
            // 1. 校验用户调用次数是否超限（保持不变）
            AiUtils.checkUserLimit(userId, redisTemplate, aiProperties.getDailyLimit());
            AiUtils.incrementUserAiCount(userId, redisTemplate);

            // 2. 过滤用户输入
            String filteredPrompt = filterUserInput(prompt);

            // 3. 保存用户提问到聊天历史
            chatHistoryRepository.saveMessage(CHAT_TYPE, userId, chatId, "user", filteredPrompt);

            // 4. 异步启动一个线程来处理 AI 回复
            return Flux.<String>create(sink -> {
                // 使用异步调用 AI 获取回复
                String assistantReply = answerChatClient.prompt()
                        .user(filteredPrompt)
                        .advisors(a -> a.param(CONVERSATION_ID, chatId))
                        .call()
                        .content();

                // 5. 过滤 AI 输出
                assistantReply = filterAIOutput(assistantReply);

                // 6. 保存 AI 回复到历史记录
                if (assistantReply != null && !assistantReply.isBlank()) {
                    chatHistoryRepository.saveMessage(CHAT_TYPE, userId, chatId, "assistant", assistantReply);
                }

                // 7. 拆分 AI 回复
                List<String> chunks = splitToChunks(assistantReply, SSE_CHUNK_SIZE);

                // 8. 每个块分段发送到前端
                chunks.forEach(chunk -> {
                    // 通过 sink 将每个块发送到 SSE
                    sink.next(toMessageEvent(chunk));
                });

                // 9. 完成后发送 "done" 事件
                sink.next(toDoneEvent());

                // 10. 完成流，关闭 sink
                sink.complete();
            }).onErrorResume(e -> {
                log.error("AI流式回复失败, userId={}, chatId={}", userId, chatId, e);
                return Flux.just(toErrorEvent("AI处理失败，请稍后重试"));
            });
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
     * 将完整文本按固定大小切片，供 SSE 分段输出
     */
    private List<String> splitToChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        int length = text.length();
        for (int start = 0; start < length; start += chunkSize) {
            int end = Math.min(start + chunkSize, length);
            chunks.add(text.substring(start, end));
        }

        return chunks;
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
