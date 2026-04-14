package com.qg.server.ai.util;

import com.qg.common.constant.AiPromptConstant;
import com.qg.common.constant.RedisConstant;
import com.qg.common.exception.AiGenerateException;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * AI 工具类：包含敏感词过滤、字段长度限制、用户调用次数管理
 */
public class AiUtils {

    /**
     * 限制字符串长度
     */
    public static String limitLength(String text) {
        if (text == null) return null;
        return text.length() > AiPromptConstant.MAX_DESCRIPTION_LENGTH
                ? text.substring(0, AiPromptConstant.MAX_DESCRIPTION_LENGTH)
                : text;
    }

    /**
     * 检查用户是否超过每日调用次数
     */
    public static void checkUserLimit(Long userId, RedisTemplate<String, Object> redisTemplate, int dailyLimit) {
        Object value = redisTemplate.opsForValue().get(RedisConstant.USER_AI_LIMIT_KEY + userId);
        if (value != null) {
            int limit = Integer.parseInt(value.toString());
            if (limit >= dailyLimit) {
                throw new AiGenerateException("您今日的AI调用次数已用完");
            }
        }
    }

    /**
     * 增加用户调用次数，首次计数设置24小时过期
     */
    public static void incrementUserAiCount(Long userId, RedisTemplate<String, Object> redisTemplate) {
        String key = RedisConstant.USER_AI_LIMIT_KEY + userId;
        Long count = redisTemplate.opsForValue().increment(key, 1);
        if (count != null && count == 1) {
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
        }
    }
}
