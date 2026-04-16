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

    /**
     * 每个 SSE 分片的最大字符数
     */
    private static final int SSE_CHUNK_SIZE = 100;  // 增大块的大小，减少前端渲染次数

    /**
     * SSE 分片发送间隔（可按体验调整）
     */
    private static final Duration SSE_CHUNK_DELAY = Duration.ofMillis(100);  // 增加延迟，减缓推送

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

    /**
     * AI SSE 流式问答接口
     *
     * @param prompt 用户输入内容
     * @param chatId 对话会话 ID（用于上下文记忆）
     * @param userId 用户 ID
     * @return Flux<String> SSE 格式的流式消息
     */
    @Override
    public Flux<String> streamAnswer(String prompt, String chatId, Long userId) {
        return Flux.defer(() -> {
            // 1. 校验用户每日调用次数是否超限
            AiUtils.checkUserLimit(userId, redisTemplate, aiProperties.getDailyLimit());

            // 2. 用户今日调用次数 +1
            AiUtils.incrementUserAiCount(userId, redisTemplate);

            // 3. 过滤用户输入（去换行、超长截断、去空格）
            String filteredPrompt = filterUserInput(prompt);

            // 4. 保存用户提问到聊天历史
            chatHistoryRepository.saveMessage(CHAT_TYPE, userId, chatId, "user", filteredPrompt);

            // 第一步：同步调用工具，获取完整的工具调用结果
            String assistantReply = answerChatClient.prompt()
                    .user(filteredPrompt)
                    .advisors(a -> a.param(CONVERSATION_ID, chatId))
                    .call()
                    .content();

            // 第二步：基于工具结果生成流式响应
            assistantReply = filterAIOutput(assistantReply);

            // 7. 保存 AI 回复到聊天历史
            if (assistantReply != null && !assistantReply.isBlank()) {
                chatHistoryRepository.saveMessage(CHAT_TYPE, userId, chatId, "assistant", assistantReply);
            }

            // 8. 切片并转换为 SSE message 事件
            List<String> chunks = splitToChunks(assistantReply, SSE_CHUNK_SIZE);

            Flux<String> messageFlux = Flux.fromIterable(chunks)
                    .delayElements(SSE_CHUNK_DELAY)
                    .map(this::toMessageEvent);

            // 9. 最后补 done 事件
            return messageFlux.concatWith(Flux.just(toDoneEvent()));
        }).onErrorResume(e -> {
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
