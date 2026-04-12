package com.qg.server.ai.client;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationOutput;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.common.constant.AiPromptConstant;
import com.qg.common.constant.BizItemAiResultStatusConstant;
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

        AiUtils.checkUserLimit(userId, redisTemplate, aiProperties.getDailyLimit());
        AiUtils.incrementUserAiCount(userId, redisTemplate);

        List<ImageAiResponseVO> results = new ArrayList<>();
        boolean hasSuccess = false; // 标记是否有成功解析的条目

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
            List<?> choices = result.getOutput() != null ? result.getOutput().getChoices() : Collections.emptyList();

            for (int i = 0; i < messages.size(); i++) {
                if (i >= choices.size() || choices.get(i) == null) {
                    log.warn("AI未返回对应图片结果，跳过，index={}", i);
                    continue; // 跳过当前条
                }

                Object choiceObj = choices.get(i);
                MultiModalMessage messageObj = null;

                try {
                    if (choiceObj instanceof Map) {
                        Map<?, ?> choiceMap = (Map<?, ?>) choiceObj;
                        Object msg = choiceMap.get("message");
                        if (msg instanceof MultiModalMessage) messageObj = (MultiModalMessage) msg;
                    } else if (choiceObj instanceof MultiModalConversationOutput.Choice) {
                        messageObj = ((MultiModalConversationOutput.Choice) choiceObj).getMessage();
                    }
                } catch (Exception ex) {
                    log.warn("解析 choice 失败，跳过，index={}", i, ex);
                    continue;
                }

                if (messageObj == null) {
                    log.warn("messageObj 为 null，跳过，index={}", i);
                    continue;
                }

                List<?> contentList = messageObj.getContent();
                if (contentList == null || contentList.isEmpty()) {
                    log.warn("content 为空，跳过，index={}", i);
                    continue;
                }

                // 遍历 content，只要有 text 就用
                String aiText = null;
                for (Object o : contentList) {
                    if (o instanceof Map) {
                        Object textObj = ((Map<?, ?>) o).get("text");
                        if (textObj != null) {
                            aiText = textObj.toString();
                            break;
                        }
                    } else if (o != null) {
                        aiText = o.toString();
                        break;
                    }
                }

                if (aiText == null) {
                    log.warn("未找到 text，跳过，index={}", i);
                    continue;
                }

                aiText = cleanAiText(aiText);

                try {
                    ImageAiResponseVO vo = objectMapper.readValue(aiText, ImageAiResponseVO.class);
                    vo.setAiCategory(AiUtils.filterSensitiveWords(vo.getAiCategory()));
                    vo.setAiTags(vo.getAiTags() != null ? AiUtils.filterSensitiveWords(vo.getAiTags()) : Collections.emptyList());
                    vo.setAiDescription(AiUtils.limitLength(AiUtils.filterSensitiveWords(vo.getAiDescription())));
                    vo.setStatus(BizItemAiResultStatusConstant.SUCCESS);
                    results.add(vo);
                    hasSuccess = true;
                } catch (Exception parseEx) {
                    log.warn("JSON解析失败，跳过，index={}", i, parseEx);
                }
            }

        } catch (Exception e) {
            log.error("AI生成失败", e);
        }

        // 如果所有条都失败，才 fallback
        if (!hasSuccess) {
            for (ImageItem item : imageItems) {
                results.add(buildFallbackVO(title, description, location));
            }
        }

        return results;
    }




//
//    {
//        "output": {
//        "choices": [
//        {
//            "finish_reason": "stop",
//                "message": {
//            "role": "assistant",
//                    "content": [       // content 是一个 Array
//            {
//                "type": "output_text",
//                    "text": "AI 生成的纯文本字符串"
//            },
//            {
//                "type": "output_image",
//                    "image": "https://..."
//            }
//          ]
//        }
//        }
//    ]
//    }
//    }






    /** 清理 AI 文本 */
    private String cleanAiText(String aiText) {
        if (aiText == null) return "";
        aiText = aiText.trim();
        if (aiText.startsWith("```json")) aiText = aiText.substring(7).trim();
        else if (aiText.startsWith("```")) aiText = aiText.substring(3).trim();
        // 只去掉开头和结尾的单个 ` 或 "
        while ((aiText.startsWith("`") && aiText.endsWith("`")) || (aiText.startsWith("\"") && aiText.endsWith("\""))) {
            aiText = aiText.substring(1, aiText.length() - 1).trim();
        }
        return aiText;
    }


    /** fallback VO */
    private ImageAiResponseVO buildFallbackVO(String title, String description, String location) {
        ImageAiResponseVO vo = new ImageAiResponseVO();
        vo.setAiCategory("未知");
        vo.setAiTags(Collections.emptyList());  // tag 不存 BizItemAiResult
        vo.setAiDescription(String.format(AiPromptConstant.DDEFAULT_DESCRIPTION, title, description, location));
        vo.setStatus(BizItemAiResultStatusConstant.FAILURE);
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
