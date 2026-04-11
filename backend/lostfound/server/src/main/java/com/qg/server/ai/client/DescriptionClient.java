package com.qg.server.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.common.constant.AiPromptConstant;
import com.qg.common.constant.BizItemAiResultStatus;
import com.qg.common.properties.AIProperties;
import com.qg.pojo.vo.ImageAiResponseVO;
import com.qg.server.ai.util.AiUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class DescriptionClient {

    private final ChatClient chatClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AIProperties aiProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成物品描述 VO
     */
    public ImageAiResponseVO generateDescriptionVo(String title, String description, String location, Long userId) {
        AiUtils.checkUserLimit(userId, redisTemplate, aiProperties.getDailyLimit());
        AiUtils.incrementUserAiCount(userId, redisTemplate);

        String prompt = buildPrompt(title, description, location);
        ImageAiResponseVO response = new ImageAiResponseVO();
        try {
            String aiResponse = chatClient.prompt().user(prompt).call().content();
            response = objectMapper.readValue(aiResponse, ImageAiResponseVO.class);
            response.setAiCategory(AiUtils.filterSensitiveWords(response.getAiCategory()));
            // 对每个 tag 也过滤敏感词
            if (response.getAiTags() != null) {
                response.setAiTags(
                        response.getAiTags().stream()
                                .map(AiUtils::filterSensitiveWords)
                                .toList()
                );
            }
            response.setAiDescription(AiUtils.limitLength(AiUtils.filterSensitiveWords(response.getAiDescription())));
            response.setStatus(BizItemAiResultStatus.SUCCESS);
        } catch (Exception e) {
            log.error("AI生成描述失败", e);
            response.setAiCategory("未知");
            response.setAiTags(Collections.emptyList());
            response.setAiDescription(String.format(AiPromptConstant.DEFAULT_DESCRIPTION_TEMPLATE, title));
            response.setStatus(BizItemAiResultStatus.FAILURE);
        }
        return response;
    }


    private String buildPrompt(String title, String description, String location) {
        return String.format(
                "请生成一段详细的失物描述，物品名称：%s，用户描述：%s，丢失地点：%s。长度不超过 %d 字，禁止输出敏感信息。",
                AiUtils.filterSensitiveWords(title),
                AiUtils.filterSensitiveWords(description),
                AiUtils.filterSensitiveWords(location),
                AiPromptConstant.MAX_DESCRIPTION_LENGTH
        );
    }
}
