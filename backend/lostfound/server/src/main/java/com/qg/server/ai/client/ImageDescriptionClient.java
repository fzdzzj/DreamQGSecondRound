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

/**
 * AI 图片描述客户端
 * 多图片生成 AI 分类、标签和描述
 * 说明：
 *  - 生成的 aiTags 不再存 BizItemAiResult 表，而是单独插入 BizItemAiTag 表
 *  - 支持多图片输入，每张图片单独生成一个 VO
 */
@Slf4j
@RequiredArgsConstructor
public class ImageDescriptionClient {

    private final AIProperties aiProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 多图片生成描述
     */
    public List<ImageAiResponseVO> generateDescriptionVo(
            String title, String description, String location, Long userId, List<ImageItem> imageItems) {

        // 用户限制检查
        AiUtils.checkUserLimit(userId, redisTemplate, aiProperties.getDailyLimit());
        AiUtils.incrementUserAiCount(userId, redisTemplate);

        List<ImageAiResponseVO> results = new ArrayList<>();

        try {
            MultiModalConversation conv = new MultiModalConversation();
            List<MultiModalMessage> messages = new ArrayList<>();

            // 构建每张图片的 prompt
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

            List<?> choices = result.getOutput() != null ? result.getOutput().getChoices() : Collections.emptyList();

            for (int i = 0; i < messages.size(); i++) {
                if (i >= choices.size() || choices.get(i) == null) {
                    // AI 没有返回对应图片的结果
                    results.add(buildFallbackVO(title, description, location));
                    continue;
                }

                Object choiceObj = choices.get(i);

                // 这里假设每个 choiceObj 实际是 Map 或可通过 get("message") 获取内容
                Map<?, ?> choiceMap;
                if (choiceObj instanceof Map) {
                    choiceMap = (Map<?, ?>) choiceObj;
                } else {
                    results.add(buildFallbackVO(title, description, location));
                    continue;
                }

                Object messageObj = choiceMap.get("message"); // ⚡ 根据 SDK 实际结构获取
                if (!(messageObj instanceof Map)) {
                    results.add(buildFallbackVO(title, description, location));
                    continue;
                }

                Map<?, ?> messageMap = (Map<?, ?>) messageObj;
                List<?> contentList = (List<?>) messageMap.get("content");

                if (contentList == null || contentList.isEmpty()) {
                    results.add(buildFallbackVO(title, description, location));
                    continue;
                }

                // 拼接文本
                StringBuilder aiTextBuilder = new StringBuilder();
                for (Object o : contentList) {
                    if (o instanceof Map) {
                        Object textObj = ((Map<?, ?>) o).get("text");
                        if (textObj != null) aiTextBuilder.append(textObj.toString());
                    } else {
                        aiTextBuilder.append(o.toString());
                    }
                }

                String aiText = cleanAiText(aiTextBuilder.toString());

                try {
                    ImageAiResponseVO vo = objectMapper.readValue(aiText, ImageAiResponseVO.class);
                    vo.setAiCategory(AiUtils.filterSensitiveWords(vo.getAiCategory()));
                    vo.setAiTags(vo.getAiTags() != null ? AiUtils.filterSensitiveWords(vo.getAiTags()) : Collections.emptyList());
                    vo.setAiDescription(AiUtils.limitLength(AiUtils.filterSensitiveWords(vo.getAiDescription())));
                    vo.setStatus("SUCCESS");
                    results.add(vo);
                } catch (Exception parseEx) {
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




    /** 清理 AI 文本 */
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

    /** fallback VO */
    private ImageAiResponseVO buildFallbackVO(String title, String description, String location) {
        ImageAiResponseVO vo = new ImageAiResponseVO();
        vo.setAiCategory("未知");
        vo.setAiTags(Collections.emptyList());  // tag 不存 BizItemAiResult
        vo.setAiDescription(String.format(AiPromptConstant.DEFAULT_DESCRIPTION_TEMPLATE, title, description, location));
        vo.setStatus("FAILURE");
        return vo;
    }

    /** 图片对象 */
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
