package com.qg.server.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.common.constant.AiPromptConstant;
import com.qg.common.constant.BizItemAiResultStatusConstant;
import com.qg.common.properties.AIProperties;
import com.qg.common.util.SensitiveWordFilterUtil;
import com.qg.pojo.vo.ImageAiResponseVO;
import com.qg.server.ai.util.AiUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;

/**
 * 物品描述AI
 * 1. 生成物品描述 VO
 * 2. 检查用户是否超过每日限制
 * 3. 增加用户AI次数
 * 4. 调用AI生成描述
 * 5. 过滤敏感词
 * 6. 限制描述长度
 */
@Slf4j
@RequiredArgsConstructor
public class DescriptionClient {

    private final ChatClient chatClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AIProperties aiProperties;
    private final SensitiveWordFilterUtil sensitiveWordFilterUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成物品描述 VO
     * 1. 检查用户是否超过每日限制
     * 2. 增加用户AI次数
     * 3. 调用AI生成描述
     * 4. 过滤敏感词
     * 5. 限制描述长度
     * 6. 设置状态为成功
     */
    public ImageAiResponseVO generateDescriptionVo(String title, String description, String location, Long userId) {
        //1. 检查用户是否超过每日限制
        AiUtils.checkUserLimit(userId, redisTemplate, aiProperties.getDailyLimit());
        //2. 增加用户AI次数
        AiUtils.incrementUserAiCount(userId, redisTemplate);
        //3. 调用AI生成描述
        String prompt = buildPrompt(title, description, location);
        ImageAiResponseVO response = new ImageAiResponseVO();
        try {
            String aiResponse = chatClient.prompt().user(prompt).call().content();
            response = objectMapper.readValue(aiResponse, ImageAiResponseVO.class);
            //4. 过滤敏感词
            response.setAiCategory(sensitiveWordFilterUtil.filter(response.getAiCategory()));
            // 对每个 tag 也过滤敏感词
            if (response.getAiTags() != null) {
                response.setAiTags(
                        response.getAiTags().stream()
                                .map(sensitiveWordFilterUtil::filter)
                                .toList()
                );
            }
            //5. 限制描述长度
            response.setAiDescription(AiUtils.limitLength(sensitiveWordFilterUtil.filter(response.getAiDescription())));
            response.setStatus(BizItemAiResultStatusConstant.SUCCESS);
        } catch (Exception e) {
            log.error("AI生成描述失败", e);
            response = buildFallbackVO(title, description, location);
        }
        //6. 设置状态为成功
        return response;
    }

    /**
     * 构建AI提示
     */
    private String buildPrompt(String title, String description, String location) {
        return String.format(
                "请生成一段详细的失物描述，物品名称：%s，用户描述：%s，丢失地点：%s。长度不超过 %d 字，禁止输出敏感信息。",
                sensitiveWordFilterUtil.filter(title),
                sensitiveWordFilterUtil.filter(description),
                sensitiveWordFilterUtil.filter(location),
                AiPromptConstant.MAX_DESCRIPTION_LENGTH
        );
    }

    /**
     * 构建失败的VO
     */
    private ImageAiResponseVO buildFallbackVO(String title, String description, String location) {
        ImageAiResponseVO vo = new ImageAiResponseVO();
        vo.setAiCategory("未知");
        vo.setAiTags(Collections.emptyList());
        vo.setAiDescription(String.format(AiPromptConstant.DDEFAULT_DESCRIPTION, title, description, location));
        vo.setStatus(BizItemAiResultStatusConstant.FAILURE);
        return vo;
    }
}
