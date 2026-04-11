package com.qg.server.service.impl;

import com.qg.common.constant.AiPromptConstant;
import com.qg.common.constant.BizItemAiResultStatus;
import com.qg.common.properties.AIProperties;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizItemAiResult;
import com.qg.pojo.vo.ImageAiResponseVO;
import com.qg.server.ai.client.DescriptionClient;
import com.qg.server.ai.client.ImageDescriptionClient;
import com.qg.server.ai.client.ImageDescriptionClient.ImageItem;
import com.qg.server.mapper.BizItemAiResultDao;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.service.AiAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiAsyncServiceImpl implements AiAsyncService {

    private final DescriptionClient descriptionClient;
    private final ImageDescriptionClient imageDescriptionClient;
    private final BizItemAiResultDao aiResultDao;
    private final BizItemDao itemDao;
    private final AIProperties aiProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 文本生成
     */
    @Override
    @Transactional
    public void generateItemDescription(String title, String description, String location, Long userId, Long itemId) {
        ImageAiResponseVO generatedDesc = descriptionClient.generateDescriptionVo(title, description, location, userId);

        String status = BizItemAiResultStatus.SUCCESS.equals(generatedDesc.getStatus())
                ? BizItemAiResultStatus.SUCCESS
                : BizItemAiResultStatus.FAILURE;

        Long resultId = persistAiDescription(title, location, userId, itemId, generatedDesc.getAiDescription(), description, status, generatedDesc.getAiCategory(), generatedDesc.getAiTags());
        updateItemCurrentAiResultId(itemId, resultId);
    }

    /**
     * 图片多模态生成
     */
    @Override
    @Transactional
    public void generateItemImageDescription(String title, String description, String location,
                                             Long userId, Long itemId, List<ImageItem> imageItems) {
        log.info("收到图片多模态生成事件, itemId={}, imageItems={}", itemId, imageItems);
        List<ImageAiResponseVO> results = imageDescriptionClient.generateDescriptionVo(
                title, description, location, userId, imageItems
        );

        if (results.isEmpty()) return;

        // ✅ 获取最新 version + 1，所有图片使用同一版本
        BizItemAiResult lastResult = aiResultDao.selectLatestByItemId(itemId);
        int newVersion = (lastResult == null ? 1 : lastResult.getResultVersion() + 1);

        Long lastResultId = null;
        for (ImageAiResponseVO vo : results) {
            BizItemAiResult result = new BizItemAiResult();
            result.setItemId(itemId);
            result.setResultVersion(newVersion); // 同一批共用 version
            result.setSourceType(lastResult == null ? "AUTO" : "REGENERATE");
            result.setPromptText(title + " " + location + " " + description);
            result.setOriginText(description);
            result.setAiDescription(vo.getAiDescription());
            result.setModelName(aiProperties.getModel());
            result.setStatus(vo.getStatus());
            result.setIsDeleted(0);
            result.setAiCategory(vo.getAiCategory() != null ? vo.getAiCategory() : "未知");
            result.setAiTags(vo.getAiTags() != null ? vo.getAiTags() : "");
            if (lastResult == null) {
                result.setCreateUser(userId);
            } else {
                result.setCreateUser(lastResult.getCreateUser());
            }
            result.setUpdateUser(userId);

            aiResultDao.insert(result);
            lastResultId = result.getId();
        }

        if (lastResultId != null) {
            updateItemCurrentAiResultId(itemId, lastResultId);
        }
    }


    /**
     * 持久化 AI 结果
     */
    @Override
    @Transactional
    public Long persistAiDescription(String title, String location, Long userId, Long itemId,
                                     String generatedDesc, String originDesc, String status,
                                     String aiCategory, String aiTags) {

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

        // ✅ 设置分类和标签，保证不为空
        result.setAiCategory(aiCategory != null ? aiCategory : "未知");
        result.setAiTags(aiTags != null ? aiTags : "");

        if (lastResult == null) {
            result.setCreateUser(userId);
        }else{
            result.setCreateUser(lastResult.getCreateUser());
        }
        result.setUpdateUser(userId);

        aiResultDao.insert(result);
        return result.getId();
    }


    /**
     * 更新 item 的 currentAiResultId
     */
    @Override
    @Transactional
    public void updateItemCurrentAiResultId(Long itemId, Long aiResultId) {
        BizItem item = itemDao.selectById(itemId);
        if (item != null) {
            item.setCurrentAiResultId(aiResultId);
            itemDao.updateById(item);
        }
        // 清理缓存
        evictItemCaches(itemId);
    }

    private void evictItemCaches(Long itemId) {
        redisTemplate.delete("ITEM_DETAIL_KEY:" + itemId);
        Set<String> keys = redisTemplate.keys("ITEM_PAGE_KEY:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
