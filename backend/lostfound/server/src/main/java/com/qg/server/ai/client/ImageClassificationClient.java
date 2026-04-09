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

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageClassificationClient {

    private final ChatClient chatClient;
    private final AIProperties aiProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImageAiResponseVO classifyAndDescribe(String title, String description, String location, Long userId, Long itemId) {
        AiUtils.checkUserLimit(userId, redisTemplate, aiProperties.getDailyLimit());
        AiUtils.incrementUserAiCount(userId, redisTemplate);

        ImageAiResponseVO response = new ImageAiResponseVO();
        try {
            String prompt = buildPrompt(itemId, title, description, location);
            String aiResponse = chatClient.prompt().user(prompt).call().content();

            // 先解析 JSON
            response = objectMapper.readValue(aiResponse, ImageAiResponseVO.class);

            // 字段安全处理
            response.setAiCategory(AiUtils.filterSensitiveWords(response.getAiCategory()));
            response.setAiTags(AiUtils.filterSensitiveWords(response.getAiTags()));
            response.setAiDescription(AiUtils.limitLength(AiUtils.filterSensitiveWords(response.getAiDescription())));

            response.setStatus(BizItemAiResultStatus.SUCCESS);
        } catch (Exception e) {
            log.error("AI图片分类失败, itemId={}, userId={}", itemId, userId, e);
            response.setAiCategory("未知");
            response.setAiTags("");
            response.setAiDescription(String.format(AiPromptConstant.DEFAULT_DESCRIPTION_TEMPLATE, title));
            response.setStatus(BizItemAiResultStatus.FAILURE);
        }
        return response;
    }

    private String buildPrompt(Long itemId, String title, String description, String location) {
        String imageUrl = "https://cdn.example.com/images/" + itemId;
        return String.format(AiPromptConstant.DEFAULT_IMAGE_CLASSIFICATION_TEMPLATE,
                AiUtils.filterSensitiveWords(imageUrl),
                AiUtils.filterSensitiveWords(title),
                AiUtils.filterSensitiveWords(description),
                AiUtils.filterSensitiveWords(location));
    }
}
