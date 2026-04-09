package com.qg.server.ai.client;

import com.qg.common.constant.AiPromptConstant;
import com.qg.common.constant.RedisConstant;
import com.qg.common.exception.AiGenerateException;
import com.qg.common.properties.AIProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DescriptionClient {

    private final ChatClient chatClient;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private AIProperties aiProperties;

    public DescriptionClient(ChatClient chatClient){
        this.chatClient = chatClient;
    }

    /**
     * 生成物品描述，带内容过滤、长度限制、默认描述
     */
    public String generateDescription(String title, String description, String location,Long userId) {
        // 1. 检查用户是否超过每日限制
        Long limit = (Long) redisTemplate.opsForValue().get(RedisConstant.USER_AI_LIMIT_KEY + userId);

        if (limit!= null && limit >= aiProperties.getDailyLimit()) {
            throw new AiGenerateException("您今日的AI调用次数已用完");
        }
        // 2. 累加用户使用次数
        incrementUserAiCount(userId);
        log.info("用户使用ai次数加一，userId={}, limit={}", userId, limit+1);
        String prompt = String.format(
                "请生成一段详细的失物描述，物品名称：%s，用户描述：%s，丢失地点：%s。",
                title, description, location
        );

        try {
            String aiResponse = chatClient.prompt()
                    .user( prompt)
                    .call()
                    .content();

            // 1. 敏感词过滤
            aiResponse = filterSensitiveWords(aiResponse);

            // 2. 内容长度限制
            aiResponse = limitLength(aiResponse);

            return aiResponse;
        } catch (Exception e) {
            log.error("AI生成描述失败, title={}, location={}", title, location, e);
            // 失败返回默认描述
            return String.format(AiPromptConstant.DEFAULT_DESCRIPTION_TEMPLATE, title);
        }
    }

    private String filterSensitiveWords(String text) {
        for(String word : AiPromptConstant.SENSITIVE_WORDS){
            text = text.replaceAll(word, "***");
        }
        return text;
    }

    private String limitLength(String text) {
        return text.length() > AiPromptConstant.MAX_DESCRIPTION_LENGTH
                ? text.substring(0, AiPromptConstant.MAX_DESCRIPTION_LENGTH)
                : text;
    }
    private void incrementUserAiCount(Long userId) {
        String key = RedisConstant.USER_AI_LIMIT_KEY + userId;
        Long count = redisTemplate.opsForValue().increment(key, 1);

        if(count != null && count == 1) {
            // 第一次计数，设置 24 小时过期
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
        }

        log.info("用户使用AI次数加一，userId={}, count={}", userId, count);
    }

}
