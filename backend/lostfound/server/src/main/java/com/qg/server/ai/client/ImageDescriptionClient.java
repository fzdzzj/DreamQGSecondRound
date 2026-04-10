package com.qg.server.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.common.constant.AiPromptConstant;
import com.qg.common.properties.AIProperties;
import com.qg.pojo.vo.ImageAiResponseVO;
import com.qg.server.ai.util.AiUtils;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ImageDescriptionClient {

    private final AIProperties aiProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 多图片、多类型物品生成描述
     *
     * @param title      物品名称
     * @param description 用户描述
     * @param location   丢失地点
     * @param userId     用户ID
     * @param imageItems 图片列表，每个 ImageItem 包含 URL 和类型
     * @return List<ImageAiResponseVO> 每张图片或每类图片对应一个 VO
     */
    public List<ImageAiResponseVO> generateDescriptionVo(String title, String description, String location, Long userId, List<ImageItem> imageItems) {
        log.info("client generateDescriptionVo, userId={}, imageItems={}", userId, imageItems);
        AiUtils.checkUserLimit(userId, redisTemplate, aiProperties.getDailyLimit());
        AiUtils.incrementUserAiCount(userId, redisTemplate);
        List<ImageAiResponseVO> results = new ArrayList<>();
        log.info("client generateDescriptionVo, userId={}, imageItems={}", userId, imageItems);

        try {
            log.info("client generateDescriptionVo, userId={}, imageItems={}", userId, imageItems);
            MultiModalConversation conv = new MultiModalConversation();
            List<MultiModalMessage> messages = new ArrayList<>();

            for (ImageItem item : imageItems) {
                MultiModalMessage userMessage = MultiModalMessage.builder()
                        .role(Role.USER.getValue())
                        .content(new ArrayList<>())
                        .build();
                userMessage.getContent().add(Collections.singletonMap("image", item.getUrl()));
                userMessage.getContent().add(Collections.singletonMap("text", String.format(AiPromptConstant.IMAGE_DESCRIPTION_PROMPT, title, description, location) + " 类型：" + item.getType()));
                messages.add(userMessage);
            }

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(aiProperties.getApiKey())
                    .model("qwen3.6-plus")
                    .messages(messages)
                    .build();

            MultiModalConversationResult result = conv.call(param);
            for (int i = 0; i < messages.size(); i++) {
                String aiText = result.getOutput().getChoices().get(i).getMessage().getContent().get(0).get("text").toString();
                ImageAiResponseVO vo = objectMapper.readValue(aiText, ImageAiResponseVO.class);
                vo.setAiCategory(AiUtils.filterSensitiveWords(vo.getAiCategory()));
                vo.setAiTags(AiUtils.filterSensitiveWords(vo.getAiTags()));
                vo.setAiDescription(AiUtils.limitLength(AiUtils.filterSensitiveWords(vo.getAiDescription())));
                results.add(vo);
            }
        } catch (Exception e) {
            log.error("AI图片生成描述失败", e);
            for (ImageItem item : imageItems) {
                ImageAiResponseVO vo = new ImageAiResponseVO();
                vo.setAiCategory("未知");
                vo.setAiTags("");
                vo.setAiDescription(String.format(AiPromptConstant.DEFAULT_DESCRIPTION_TEMPLATE, title, description, location));
                results.add(vo);
            }
        }
        log.info("client generateDescriptionVo, userId={}, imageItems={}, results={}", userId, imageItems, results);
        return results;
    }


    /**
     * 图片对象
     */
    public static class ImageItem {
        private String url;
        private String type; // 正面/背面/配件 等

        public ImageItem(String url, String type) {
            this.url = url;
            this.type = type;
        }
        public String getUrl() { return url; }
        public String getType() { return type; }
    }
}
