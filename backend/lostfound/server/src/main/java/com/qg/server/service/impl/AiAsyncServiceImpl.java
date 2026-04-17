package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.common.constant.BizItemAiResultStatusConstant;
import com.qg.common.constant.MessageConstant;
import com.qg.common.constant.RedisConstant;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.AiGenerateException;
import com.qg.common.exception.BaseException;
import com.qg.common.exception.UpdateNotAllowedException;
import com.qg.common.properties.AIProperties;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizItemAiResult;
import com.qg.pojo.entity.BizItemAiTag;
import com.qg.pojo.entity.BizItemImage;
import com.qg.pojo.vo.ImageAiResponseVO;
import com.qg.server.ai.client.DescriptionClient;
import com.qg.server.ai.client.ImageDescriptionClient;
import com.qg.server.mapper.BizItemAiResultDao;
import com.qg.server.mapper.BizItemAiTagDao;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.mapper.BizItemImageDao;
import com.qg.server.service.AiAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI异步服务实现类
 * 用于处理异步的AI生成任务，如物品描述、物品图片描述等。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiAsyncServiceImpl implements AiAsyncService {

    private final DescriptionClient descriptionClient;
    private final ImageDescriptionClient imageDescriptionClient;
    private final BizItemAiResultDao aiResultDao;
    private final BizItemImageDao itemImageDao;
    private final BizItemAiTagDao aiTagDao;
    private final BizItemDao itemDao;
    private final AIProperties aiProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationContext applicationContext;
    private AiAsyncServiceImpl getSelf() {
        return applicationContext.getBean(AiAsyncServiceImpl.class);
    }

    /**
     * 文本生成物品描述异步任务
     */
    @Override
    public void generateItemDescription(String title, String description, String location,
                                        Long userId, Long itemId) {
        // 1. 调用AI模型（无事务！！！）
        ImageAiResponseVO generatedDesc = descriptionClient.generateDescriptionVo(title, description, location, userId);
        String status = BizItemAiResultStatusConstant.SUCCESS.equals(generatedDesc.getStatus())
                ? BizItemAiResultStatusConstant.SUCCESS
                : BizItemAiResultStatusConstant.FAILURE;

        // 2. 多个DB操作，交给事务方法保证原子性
        getSelf().saveAiResultAndTags(itemId, userId, generatedDesc, title, location, description, status);
    }

    /**
     * 事务方法：仅保存AI结果和标签，原子性操作
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAiResultAndTags(Long itemId, Long userId,
                                    ImageAiResponseVO generatedDesc,
                                    String title, String location,
                                    String originDesc, String status) {
        // 保存描述
        Map<String, String> resultInfo = persistAiDescription(title, location, userId, itemId,
                generatedDesc.getAiDescription(), originDesc, status, generatedDesc.getAiCategory());

        // 保存标签
        if (generatedDesc.getAiTags() != null && !generatedDesc.getAiTags().isEmpty()) {
            insertAiTags(itemId, Integer.parseInt(resultInfo.get("resultId")), generatedDesc.getAiTags());
        }

        // 更新物品表
        updateItemCurrentAiResultId(itemId, Long.parseLong(resultInfo.get("resultId")), status);
    }

    /**
     * 图片多模态生成物品图片描述异步任务
     */
    @Override
    public void generateItemImageDescription(String title, String description, String location,
                                             Long userId, Long itemId, List<ImageDescriptionClient.ImageItem> imageItems) {
        // 1. 调用AI模型（无事务）
        List<ImageAiResponseVO> results = imageDescriptionClient.generateItemImageDescription(title, description, location, userId, imageItems);
        if (results.isEmpty()) return;

        // 2. 批量保存DB，交给事务方法
        getSelf().batchSaveAiResults(itemId, userId, title, location, description, results);
    }

    /**
     * 【事务方法】批量保存AI结果，原子性
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveAiResults(Long itemId, Long userId,
                                   String title, String location, String description,
                                   List<ImageAiResponseVO> results) {
        BizItemAiResult lastResult = aiResultDao.selectLatestByItemId(itemId);
        int newVersion = (lastResult == null ? 1 : lastResult.getResultVersion() + 1);

        Long lastResultId = null;
        boolean allSuccess = true;
        String isTimeout = (String) redisTemplate.opsForValue().get("AI_TIMEOUT:" + itemId);
        // 检查是否超时
        if ("true".equals(isTimeout)) {
            log.warn("[AI] 已超时，跳过入库 itemId:{}", itemId);
            return;
        }
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
            result.setImageUrls(vo.getImageUrls());

            aiResultDao.insert(result);
            lastResultId = result.getId();

            // 插入 tag
            if (vo.getAiTags() != null && !vo.getAiTags().isEmpty()) {
                insertAiTags(itemId, newVersion, vo.getAiTags());
            }

            if (!BizItemAiResultStatusConstant.SUCCESS.equals(vo.getStatus())) {
                allSuccess = false;
            }
        }

        if (lastResultId != null) {
            updateItemCurrentAiResultId(itemId, lastResultId,
                    allSuccess ? BizItemAiResultStatusConstant.SUCCESS : BizItemAiResultStatusConstant.FAILURE);
        }
    }

    /**
     * 重新生成物品描述异步任务
     */
    @Override
    public void regenerateItemDescription(Long itemId, Long userId) {
        log.info("重新生成物品描述,用户ID:{},物品ID:{}", userId, itemId);

        // 1. 查询物品
        BizItem item = itemDao.selectById(itemId);
        if (item == null || (item.getDeleted() != null && item.getDeleted() == 1)) {
            log.warn("物品不存在,或已删除,物品ID:{}", itemId);
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        // 2. 权限判断
        if (!item.getUserId().equals(userId)) {
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        }

        // 3. 判断是否有AI记录
        BizItemAiResult latestResult = aiResultDao.selectLatestByItemId(itemId);
        if (latestResult == null) {
            throw new AiGenerateException(MessageConstant.AI_RESULT_NOT_FOUND);
        }

        // 4. 更新状态为处理中
        updateItemStatusToPending(itemId);

        // 5. 获取图片
        List<BizItemImage> imageList = itemImageDao.selectList(new LambdaQueryWrapper<BizItemImage>()
                .eq(BizItemImage::getItemId, itemId));
        List<ImageDescriptionClient.ImageItem> imageItems = imageList.stream()
                .map(img -> new ImageDescriptionClient.ImageItem(img.getUrl(), null)).toList();

        // 6. 调用AI生成（无事务）
        if (!imageItems.isEmpty()) {
            generateItemImageDescription(item.getTitle(), item.getDescription(), item.getLocation(), userId, itemId, imageItems);
        } else {
            generateItemDescription(item.getTitle(), item.getDescription(), item.getLocation(), userId, itemId);
        }
    }

    /**
     * 单独更新状态，极简事务方法
     */
    public void updateItemStatusToPending(Long itemId) {
        String isTimeout = (String) redisTemplate.opsForValue().get("AI_TIMEOUT:" + itemId);
        if ("true".equals(isTimeout)) {
            log.warn("[AI] 已超时，跳过入库 itemId:{}", itemId);
            return;
        }
        BizItem updateItem = new BizItem();
        updateItem.setId(itemId);
        updateItem.setAiStatus(BizItemAiResultStatusConstant.PENDING);
        itemDao.updateById(updateItem);
    }

    /**
     * 保存AI描述（单insert，无需事务）
     */
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
        String isTimeout = (String) redisTemplate.opsForValue().get("AI_TIMEOUT:" + itemId);
        if ("true".equals(isTimeout)) {
            log.warn("[AI] 已超时，跳过入库 itemId:{}", itemId);
            return null;
        }
        aiResultDao.insert(result);

        Map<String, String> resultInfo = new HashMap<>();
        resultInfo.put("resultId", result.getId().toString());
        resultInfo.put("resultVersion", String.valueOf(newVersion));
        return resultInfo;
    }

    /**
     * 插入 tag 表
     */
    private void insertAiTags(Long itemId, int aiResultVersion, List<String> tags) {
        if (tags == null || tags.isEmpty()) return;
        try {
            String jsonTags = objectMapper.writeValueAsString(tags);
            BizItemAiTag t = new BizItemAiTag();
            t.setItemId(itemId);
            t.setAiResultVersion(aiResultVersion);
            t.setAiTags(jsonTags);
            String isTimeout = (String) redisTemplate.opsForValue().get("AI_TIMEOUT:" + itemId);
            if ("true".equals(isTimeout)) {
                log.warn("[AI] 已超时，跳过入库 itemId:{}", itemId);
                return;
            }
            aiTagDao.insert(t);
        } catch (Exception e) {
            log.error("序列化 AI tags 失败", e);
            throw new BaseException("序列化 AI tags 失败");
        }
    }

    /**
     * 更新 item 的当前 AI 状态
     */
    @Override
    public void updateItemCurrentAiResultId(Long itemId, Long aiResultId, String aiStatus) {
        String isTimeout = (String) redisTemplate.opsForValue().get("AI_TIMEOUT:" + itemId);
        if ("true".equals(isTimeout)) {
            log.warn("[AI] 已超时，跳过入库 itemId:{}", itemId);
            return;
        }
        BizItem item = itemDao.selectById(itemId);
        if (item != null) {
            item.setCurrentAiResultId(aiResultId);
            item.setAiStatus(aiStatus);
            itemDao.updateById(item);
            evictItemCaches(itemId);
        }
    }

    private void evictItemCaches(Long itemId) {
        redisTemplate.delete(RedisConstant.ITEM_DETAIL_KEY + itemId);
        Set<String> keys = redisTemplate.keys(RedisConstant.ITEM_PAGE_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}