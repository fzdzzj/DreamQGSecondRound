package com.qg.server.service.impl;

import com.qg.common.constant.AiPromptConstant;
import com.qg.common.constant.BizItemAiResultStatus;
import com.qg.common.constant.RedisConstant;
import com.qg.common.properties.AIProperties;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizItemAiResult;
import com.qg.server.ai.client.DescriptionClient;
import com.qg.server.ai.prompt.ItemAiPromptBuilder;
import com.qg.server.mapper.BizItemAiResultDao;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.service.AiAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiAsyncServiceImpl implements AiAsyncService {

    private final DescriptionClient descriptionClient;

    private final BizItemAiResultDao aiResultDao;

    private final BizItemDao itemDao;

    private final AIProperties aiProperties;
    private final RedisTemplate<String, Object> redisTemplate;


    @Transactional
    @Override
    public void generateItemDescription(String title, String description, String location,Long userId,Long itemId){
        String generatedDesc;

        try {
            generatedDesc = descriptionClient.generateDescription(
                    title,
                    description,
                    location,userId
                           );

            persistAiDescription(title, location, userId, itemId, generatedDesc, description, BizItemAiResultStatus.SUCCESS);



        } catch(Exception e){
            log.warn("AI生成异常, 使用默认描述, itemId={}, userId={}", itemId, userId, e);
            generatedDesc = String.format(AiPromptConstant.DEFAULT_DESCRIPTION_TEMPLATE, title);
            persistAiDescription(title, location, userId, itemId, generatedDesc, description, BizItemAiResultStatus.FAILURE);
        }
    }

    private void persistAiDescription(String title,String location,Long userId, Long itemId, String generatedDesc,String originDesc, String status){
        BizItemAiResult lastResult = aiResultDao.selectLatestByItemId(itemId);
        int newVersion = (lastResult == null ? 1 : lastResult.getResultVersion() + 1);

        BizItemAiResult result = new BizItemAiResult();
        result.setItemId(itemId);
        result.setResultVersion(newVersion);
        result.setSourceType(lastResult == null ? "AUTO" : "REGENERATE");
        result.setPromptText(title + " " + location+" "+originDesc);
        result.setOriginText(originDesc);
        result.setAiDescription(generatedDesc);
        result.setModelName(aiProperties.getModel());
        result.setStatus(status);
        result.setIsDeleted(0);
        if(lastResult== null){
        result.setCreateUser(userId);
        }
        result.setUpdateUser(userId);
        if(lastResult== null){
            result.setCreateUser(userId);
        }
        result.setUpdateUser(userId);
        aiResultDao.insert(result);

        // 同步更新 BizItem
        BizItem item = itemDao.selectById(itemId);
        if(item != null){
            item.setCurrentAiResultId(result.getId());
            item.setAiStatus(status);
            item.setAiTags(result.getAiTags());
            itemDao.updateById(item);
        }
        evictItemCaches(itemId);

    }
    /**
     * 处理 null 字符串，避免缓存 key 中出现 null 文本干扰判断
     */
    private String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * 清理物品相关缓存
     */
    private void evictItemCaches(Long itemId) {
        redisTemplate.delete(RedisConstant.ITEM_DETAIL_KEY + itemId);

        Set<String> keys = redisTemplate.keys(RedisConstant.ITEM_PAGE_KEY + "*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
