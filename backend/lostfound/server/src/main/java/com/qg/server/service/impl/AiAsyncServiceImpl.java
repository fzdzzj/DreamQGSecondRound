package com.qg.server.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.common.properties.AIProperties;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizItemAiResult;
import com.qg.pojo.entity.BizItemAiTag;
import com.qg.pojo.vo.ImageAiResponseVO;
import com.qg.server.ai.client.DescriptionClient;
import com.qg.server.ai.client.ImageDescriptionClient;
import com.qg.server.mapper.BizItemAiResultDao;
import com.qg.server.mapper.BizItemAiTagDao;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.service.AiAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiAsyncServiceImpl implements AiAsyncService {

    private final DescriptionClient descriptionClient;
    private final ImageDescriptionClient imageDescriptionClient;
    private final BizItemAiResultDao aiResultDao;
    private final BizItemAiTagDao aiTagDao; // 新增 tag 表 DAO
    private final BizItemDao itemDao;
    private final AIProperties aiProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 文本生成
     */
    @Override
    @Transactional
    public void generateItemDescription(String title, String description, String location,
                                        Long userId, Long itemId) {
        ImageAiResponseVO generatedDesc = descriptionClient.generateDescriptionVo(title, description, location, userId);

        String status = "SUCCESS".equals(generatedDesc.getStatus()) ? "SUCCESS" : "FAILURE";

        Map<String, String> resultInfo = persistAiDescription(title, location, userId, itemId,
                generatedDesc.getAiDescription(), description, status, generatedDesc.getAiCategory());

        if (generatedDesc.getAiTags() != null && !generatedDesc.getAiTags().isEmpty()) {
            insertAiTags(itemId, Integer.parseInt(resultInfo.get("resultId")), generatedDesc.getAiTags());
        }


        updateItemCurrentAiResultId(itemId, Long.parseLong(resultInfo.get("resultId")), status);
    }

    /**
     * 图片多模态生成
     */
    @Override
    @Transactional
    public void generateItemImageDescription(String title, String description, String location,
                                             Long userId, Long itemId, List<ImageDescriptionClient.ImageItem> imageItems) {
        List<ImageAiResponseVO> results = imageDescriptionClient.generateDescriptionVo(title, description, location, userId, imageItems);
        if (results.isEmpty()) return;

        BizItemAiResult lastResult = aiResultDao.selectLatestByItemId(itemId);
        int newVersion = (lastResult == null ? 1 : lastResult.getResultVersion() + 1);

        Long lastResultId = null;
        boolean allSuccess = true;

        for (ImageAiResponseVO vo : results) {
            BizItemAiResult result = new BizItemAiResult();
            result.setItemId(itemId);
            result.setResultVersion(newVersion);
            result.setSourceType(lastResult == null ? "AUTO" : "REGENERATE");
            result.setPromptText(title + " " + location + " " + description);
            result.setOriginText(description);
            result.setAiDescription(vo.getAiDescription());
            result.setModelName(aiProperties.getModel());
            result.setStatus(vo.getStatus());
            result.setIsDeleted(0);
            result.setAiCategory(vo.getAiCategory() != null ? vo.getAiCategory() : "未知");
            result.setCreateUser(lastResult == null ? userId : lastResult.getCreateUser());
            result.setUpdateUser(userId);

            aiResultDao.insert(result);
            lastResultId = result.getId();

            // 插入 tag 表
            if (vo.getAiTags() != null && !vo.getAiTags().isEmpty()) {
                insertAiTags(itemId, newVersion, vo.getAiTags());
            }

            if (!"SUCCESS".equals(vo.getStatus())) allSuccess = false;
        }

        if (lastResultId != null) {
            // 更新 item 当前 AI 状态
            updateItemCurrentAiResultId(itemId, lastResultId, allSuccess ? "SUCCESS" : "FAILURE");
        }
    }

    /**
     * 持久化 AI 结果（不存 tags）
     */
    @Transactional
    @Override
    public Map<String, String> persistAiDescription(String title, String location, Long userId, Long itemId,
                                     String generatedDesc, String originDesc, String status,
                                     String aiCategory) {

        BizItemAiResult lastResult = aiResultDao.selectLatestByItemId(itemId);
        int newVersion = (lastResult == null ? 1 : lastResult.getResultVersion() + 1);

        BizItemAiResult result = new BizItemAiResult();
        result.setItemId(itemId);
        result.setResultVersion(newVersion);
        result.setSourceType(lastResult == null ? "AUTO" : "REGENERATE");
        result.setPromptText(title + " " + location + " " + originDesc);
        result.setOriginText(originDesc);
        result.setAiDescription(generatedDesc);
        result.setModelName(aiProperties.getModel());
        result.setStatus(status);
        result.setIsDeleted(0);
        result.setAiCategory(aiCategory != null ? aiCategory : "未知");
        result.setCreateUser(lastResult == null ? userId : lastResult.getCreateUser());
        result.setUpdateUser(userId);

        aiResultDao.insert(result);

        Map<String, String> resultInfo = new HashMap<>();
        resultInfo.put("resultId", result.getId().toString());
        resultInfo.put("resultVersion", newVersion + "");
        return resultInfo;
    }

    /**
     * 插入 tag 表
     */
    private void insertAiTags(Long itemId, int aiResultVersion, List<String> tags) {
        if (tags == null || tags.isEmpty()) return;
        try {
            String jsonTags = objectMapper.writeValueAsString(tags); // 序列化成 JSON
            BizItemAiTag t = new BizItemAiTag();
            t.setItemId(itemId);
            t.setAiResultVersion(aiResultVersion);
            t.setAiTags(jsonTags); // ⚡ 这里是 String
            aiTagDao.insert(t);
        } catch (Exception e) {
            log.error("序列化 AI tags 失败", e);
        }
    }







    /**
     * 更新 item 的当前 AI 状态
     */
    @Override
    @Transactional
    public void updateItemCurrentAiResultId(Long itemId, Long aiResultId, String aiStatus) {
        BizItem item = itemDao.selectById(itemId);
        if (item != null) {
            item.setCurrentAiResultId(aiResultId);
            item.setAiStatus(aiStatus);
            itemDao.updateById(item);
        }
        evictItemCaches(itemId);
    }

    private void evictItemCaches(Long itemId) {
        redisTemplate.delete("ITEM_DETAIL_KEY:" + itemId);
        Set<String> keys = redisTemplate.keys("ITEM_PAGE_KEY:*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
    }
}
