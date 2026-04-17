package com.qg.server.ai.client;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationOutput;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.common.constant.AiPromptConstant;
import com.qg.common.constant.BizItemAiResultStatusConstant;
import com.qg.common.properties.AIProperties;
import com.qg.common.util.AiUtils;
import com.qg.common.util.SensitiveWordFilterUtil;
import com.qg.pojo.vo.ImageAiResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 图片描述AI
 */
@Slf4j
@RequiredArgsConstructor
public class ImageDescriptionClient {

    private final AIProperties aiProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SensitiveWordFilterUtil sensitiveWordFilterUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 生成图片描述 VO
     * 1. 检查用户是否超过每日限制
     * 2. 增加用户AI次数
     * 3. 构建消息
     * 4. 调用API
     * 5. 解析结果
     * 6. 处理异常
     */
    public List<ImageAiResponseVO> generateItemImageDescription(
            String title, String description, String location, Long userId, List<ImageItem> imageItems
    ) {
        List<ImageAiResponseVO> results = new ArrayList<>();
        boolean hasSuccess = false;

        // 1. 检查用户是否超过每日限制
        AiUtils.checkUserLimit(userId, redisTemplate, aiProperties.getDailyLimit());
        // 2. 增加用户AI次数
        AiUtils.incrementUserAiCount(userId, redisTemplate);

        try {
            List<ImageAiResponseVO> tempList = new ArrayList<>();
            MultiModalConversation conv = new MultiModalConversation();
            List<MultiModalMessage> messages = new ArrayList<>();

            // 3. 构建消息
            for (ImageItem item : imageItems) {
                MultiModalMessage userMessage = MultiModalMessage.builder()
                        .role(Role.USER.getValue())
                        .content(new ArrayList<>())
                        .build();

                String type = item.type() != null ? item.type() : "主图";
                String textPrompt = String.format(
                        AiPromptConstant.IMAGE_DESCRIPTION_PROMPT,
                        title, description, location, type
                );

                userMessage.getContent().add(Collections.singletonMap("image", item.url()));
                userMessage.getContent().add(Collections.singletonMap("text", textPrompt));
                messages.add(userMessage);
            }

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(aiProperties.getApiKey())
                    .model("qwen3.6-plus")
                    .messages(messages)
                    .build();

            // 4. 调用API
            MultiModalConversationResult result = conv.call(param);

            List<?> choices = result.getOutput() != null ? result.getOutput().getChoices() : Collections.emptyList();

            // 5. 解析结果
            for (int i = 0; i < messages.size(); i++) {
                if (i >= choices.size() || choices.get(i) == null) {
                    log.warn("AI未返回对应图片结果，跳过，index={}", i);
                    continue;
                }

                Object choiceObj = choices.get(i);
                MultiModalMessage messageObj = null;

                try {
                    if (choiceObj instanceof Map<?, ?> choiceMap) {
                        Object msg = choiceMap.get("message");
                        if (msg instanceof MultiModalMessage) {
                            messageObj = (MultiModalMessage) msg;
                        }
                    } else if (choiceObj instanceof MultiModalConversationOutput.Choice) {
                        messageObj = ((MultiModalConversationOutput.Choice) choiceObj).getMessage();
                    }
                } catch (Exception ex) {
                    log.warn("解析choice失败，跳过，index={}", i, ex);
                    continue;
                }

                if (messageObj == null) {
                    log.warn("messageObj为空，跳过，index={}", i);
                    continue;
                }

                List<?> contentList = messageObj.getContent();
                if (contentList == null || contentList.isEmpty()) {
                    log.warn("content为空，跳过，index={}", i);
                    continue;
                }

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
                    log.warn("未找到text，跳过，index={}", i);
                    continue;
                }

                aiText = cleanAiText(aiText);

                try {
                    // 6. 解析JSON
                    ImageAiResponseVO vo = objectMapper.readValue(aiText, ImageAiResponseVO.class);
                    vo.setAiCategory(sensitiveWordFilterUtil.filter(vo.getAiCategory()));
                    vo.setAiTags(vo.getAiTags() != null ?
                            vo.getAiTags().stream().map(sensitiveWordFilterUtil::filter).toList()
                            : Collections.emptyList());
                    vo.setAiDescription(AiUtils.limitLength(sensitiveWordFilterUtil.filter(vo.getAiDescription())));
                    vo.setStatus(BizItemAiResultStatusConstant.SUCCESS);
                    tempList.add(vo);
                } catch (Exception parseEx) {
                    log.warn("JSON解析失败，跳过，index={}", i, parseEx);
                }
            }

            results.addAll(tempList);
            hasSuccess = !tempList.isEmpty();

        } catch (NoApiKeyException | UploadFileException e) {
            log.error("AI图片生成失败", e);
        } catch (Exception e) {
            log.error("AI图片生成失败", e);
        }

        // 兜底
        if (!hasSuccess) {
            for (ImageItem item : imageItems) {
                results.add(buildFallbackVO(title, description, location));
            }
        }

        return results;
    }

    /**
     * 清理 AI 文本
     */
    private String cleanAiText(String aiText) {
        if (aiText == null) return "";
        aiText = aiText.trim();
        if (aiText.startsWith("```json")) aiText = aiText.substring(7).trim();
        else if (aiText.startsWith("```")) aiText = aiText.substring(3).trim();
        while ((aiText.startsWith("`") && aiText.endsWith("`")) || (aiText.startsWith("\"") && aiText.endsWith("\""))) {
            aiText = aiText.substring(1, aiText.length() - 1).trim();
        }
        return aiText;
    }

    /**
     * fallback VO
     */
    private ImageAiResponseVO buildFallbackVO(String title, String description, String location) {
        ImageAiResponseVO vo = new ImageAiResponseVO();
        vo.setAiCategory("未知");
        vo.setAiTags(Collections.emptyList());
        vo.setAiDescription(String.format(AiPromptConstant.DDEFAULT_DESCRIPTION, title, description, location));
        vo.setStatus(BizItemAiResultStatusConstant.FAILURE);
        return vo;
    }

    /**
     * 图片对象
     */
    public record ImageItem(String url, String type) {
    }
}