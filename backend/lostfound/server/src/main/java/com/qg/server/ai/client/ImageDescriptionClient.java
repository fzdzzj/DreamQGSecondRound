package com.qg.server.ai.client;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.common.constant.AiPromptConstant;
import com.qg.common.properties.AIProperties;
import com.qg.pojo.vo.ImageAiResponseVO;
import com.qg.server.ai.util.AiUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ImageDescriptionClient {

    private final AIProperties aiProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 多图片、多类型物品生成描述
     */
    public List<ImageAiResponseVO> generateDescriptionVo(
            String title, String description, String location, Long userId, List<ImageItem> imageItems) {

        AiUtils.checkUserLimit(userId, redisTemplate, aiProperties.getDailyLimit());
        AiUtils.incrementUserAiCount(userId, redisTemplate);

        List<ImageAiResponseVO> results = new ArrayList<>();

        try {
            MultiModalConversation conv = new MultiModalConversation();
            List<MultiModalMessage> messages = new ArrayList<>();

            for (ImageItem item : imageItems) {
                MultiModalMessage userMessage = MultiModalMessage.builder()
                        .role(Role.USER.getValue())
                        .content(new ArrayList<>())
                        .build();

                String type = item.getType() != null ? item.getType() : "主图";

                String textPrompt = String.format(
                        AiPromptConstant.IMAGE_DESCRIPTION_PROMPT,
                        title, description, location, type
                );

                userMessage.getContent().add(Collections.singletonMap("image", item.getUrl()));
                userMessage.getContent().add(Collections.singletonMap("text", textPrompt));
                messages.add(userMessage);
            }

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(aiProperties.getApiKey())
                    .model("qwen3.6-plus")
                    .messages(messages)
                    .build();

            MultiModalConversationResult result = conv.call(param);

            for (int i = 0; i < messages.size(); i++) {
                List<?> contentList = result.getOutput().getChoices().get(i).getMessage().getContent();
                if (contentList == null || contentList.isEmpty()) {
                    log.warn("AI返回内容为空, 使用默认值, item index={}", i);
                    results.add(buildFallbackVO(title, description, location));
                    continue;
                }

                String aiText;
                if (contentList.get(0) instanceof Map) {
                    aiText = ((Map<?, ?>) contentList.get(0)).get("text").toString();
                } else {
                    aiText = contentList.get(0).toString();
                }

                // 清理前缀或首尾符号
                aiText = cleanAiText(aiText);

                try {
                    ImageAiResponseVO vo = objectMapper.readValue(aiText, ImageAiResponseVO.class);
                    vo.setAiCategory(AiUtils.filterSensitiveWords(vo.getAiCategory()));
                    vo.setAiTags(AiUtils.filterSensitiveWords(vo.getAiTags()));
                    vo.setAiDescription(AiUtils.limitLength(AiUtils.filterSensitiveWords(vo.getAiDescription())));
                    vo.setStatus("SUCCESS");
                    results.add(vo);
                } catch (Exception parseEx) {
                    log.warn("AI解析 JSON 失败, 使用默认值, 原始返回: {}", aiText, parseEx);
                    results.add(buildFallbackVO(title, description, location));
                }
            }

        } catch (Exception e) {
            log.error("AI图片生成描述失败", e);
            for (ImageItem item : imageItems) {
                results.add(buildFallbackVO(title, description, location));
            }
        }

        return results;
    }

    /**
     * 清理 AI 返回文本中的多余符号
     */
    private String cleanAiText(String aiText) {
        aiText = aiText.trim();
        if (aiText.startsWith("```json")) {
            aiText = aiText.substring(7).trim();
        } else if (aiText.startsWith("```")) {
            aiText = aiText.substring(3).trim();
        }
        while ((aiText.startsWith("`") && aiText.endsWith("`")) ||
                (aiText.startsWith("\"") && aiText.endsWith("\""))) {
            aiText = aiText.substring(1, aiText.length() - 1).trim();
        }
        return aiText;
    }

    /**
     * 生成 fallback VO
     */
    private ImageAiResponseVO buildFallbackVO(String title, String description, String location) {
        ImageAiResponseVO vo = new ImageAiResponseVO();
        vo.setAiCategory("未知");
        vo.setAiTags("");
        vo.setAiDescription(String.format(AiPromptConstant.DDEFAULT_DESCRIPTION, title, description, location));
        vo.setStatus("FAILURE");
        return vo;
    }

    /**
     * 图片对象
     */
    public static class ImageItem {
        private String url;
        private String type;

        public ImageItem(String url, String type) {
            this.url = url;
            this.type = type;
        }
        public String getUrl() { return url; }
        public String getType() { return type; }
    }
}
