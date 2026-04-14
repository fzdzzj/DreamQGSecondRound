package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.common.constant.BizItemAiResultStatusConstant;
import com.qg.common.constant.MessageConstant;
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
    private final BizItemAiTagDao aiTagDao; // 新增 tag 表 DAO
    private final BizItemDao itemDao;
    private final AIProperties aiProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 文本生成物品描述异步任务
     * 用于异步生成物品的描述，包括物品标题、物品描述、物品位置、用户ID、物品ID。
     *
     * @param title       物品标题
     * @param description 物品描述
     * @param location    物品位置
     * @param userId      用户ID
     * @param itemId      物品ID
     *                    1. 调用AI模型生成物品描述
     *                    2. 保存生成的描述到数据库
     *                    3. 保存物品的当前AI结果ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateItemDescription(String title, String description, String location,
                                        Long userId, Long itemId) {
        //1. 调用AI模型生成物品描述
        ImageAiResponseVO generatedDesc = descriptionClient.generateDescriptionVo(title, description, location, userId);
        //2. 保存生成的描述到数据库
        String status = BizItemAiResultStatusConstant.SUCCESS.equals(generatedDesc.getStatus()) ? BizItemAiResultStatusConstant.SUCCESS : BizItemAiResultStatusConstant.FAILURE;

        Map<String, String> resultInfo = persistAiDescription(title, location, userId, itemId,
                generatedDesc.getAiDescription(), description, status, generatedDesc.getAiCategory());
        //3. 保存物品的标签
        if (generatedDesc.getAiTags() != null && !generatedDesc.getAiTags().isEmpty()) {
            insertAiTags(itemId, Integer.parseInt(resultInfo.get("resultId")), generatedDesc.getAiTags());
        }
        //4. 保存物品的当前AI结果ID
        updateItemCurrentAiResultId(itemId, Long.parseLong(resultInfo.get("resultId")), status);
    }

    /**
     * 图片多模态生成物品图片描述异步任务
     * 用于异步生成物品的图片描述，包括物品标题、物品描述、物品位置、用户ID、物品ID、图片列表。
     *
     * @param title       物品标题
     * @param description 物品描述
     * @param location    物品位置
     * @param userId      用户ID
     * @param itemId      物品ID
     * @param imageItems  图片列表
     *                    1. 调用AI模型生成物品图片描述
     *                    2. 保存生成的图片描述到数据库
     *                    3. 插入tag
     *                    4. 更新ai结果ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateItemImageDescription(String title, String description, String location,
                                             Long userId, Long itemId, List<ImageDescriptionClient.ImageItem> imageItems) {
        //1. 调用AI模型生成物品图片描述
        List<ImageAiResponseVO> results = imageDescriptionClient.generateDescriptionVo(title, description, location, userId, imageItems);
        // 2. 保存生成的图片描述到数据库
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
            result.setImageUrls(vo.getImageUrls());

            aiResultDao.insert(result);
            lastResultId = result.getId();

            // 3.插入 tag 表
            if (vo.getAiTags() != null && !vo.getAiTags().isEmpty()) {
                insertAiTags(itemId, newVersion, vo.getAiTags());
            }

            if (!BizItemAiResultStatusConstant.SUCCESS.equals(vo.getStatus())) allSuccess = false;
        }

        if (lastResultId != null) {
            // 4.更新 item 当前 AI 状态
            updateItemCurrentAiResultId(itemId, lastResultId, allSuccess ? BizItemAiResultStatusConstant.SUCCESS : BizItemAiResultStatusConstant.FAILURE);
        }
    }

    /**
     * 重新生成物品描述异步任务
     *
     * @param itemId
     * @param userId 1. 获取物品描述
     *               2. 先把主表中的AI状态改为处理中
     *               3. 调用AI模型生成物品描述
     */
    @Override
    public void regenerateItemDescription(Long itemId, Long userId) {
        log.info("重新生成物品描述,用户ID:{},物品ID:{}", userId, itemId);
        //1. 获取物品描述
        BizItem item = itemDao.selectById(itemId);
        if (item == null || item.getDeleted() != null && item.getDeleted() == 1) {
            log.warn("物品不存在,或已删除,物品ID:{}", itemId);
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }
        log.info("重新生成物品描述,用户ID:{},物品ID:{},物品用户ID:{}", userId, itemId, item.getUserId());
        if (!item.getUserId().equals(userId)) {
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        }
        BizItemAiResult latestResult = aiResultDao.selectLatestByItemId(itemId);
        if (latestResult == null) {
            throw new AiGenerateException(MessageConstant.AI_RESULT_NOT_FOUND);
        }
        //2.先把主表状态改成处理中
        BizItem updateItem = new BizItem();
        updateItem.setId(itemId);
        updateItem.setAiStatus(BizItemAiResultStatusConstant.PENDING);
        itemDao.updateById(updateItem);

        List<BizItemImage> imageList = itemImageDao.selectList(new LambdaQueryWrapper<BizItemImage>()
                .eq(BizItemImage::getItemId, itemId));

        List<ImageDescriptionClient.ImageItem> imageItems = imageList.stream()
                .map(img -> new ImageDescriptionClient.ImageItem(img.getUrl(), null)).toList();

        //3. 有图走多模态，无图走文本
        if (imageItems != null && !imageItems.isEmpty()) {
            generateItemImageDescription(
                    item.getTitle(),
                    item.getDescription(),
                    item.getLocation(),
                    userId,
                    itemId,
                    imageItems
            );
        } else {
            generateItemDescription(
                    item.getTitle(),
                    item.getDescription(),
                    item.getLocation(),
                    userId,
                    itemId
            );
        }
    }

    /**
     *
     * @param title
     * @param location
     * @param userId
     * @param itemId
     * @param generatedDesc
     * @param originDesc
     * @param status
     * @param aiCategory
     * @return 1. 插入结果表
     * 2. 返回插入结果ID和版本号
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, String> persistAiDescription(String title, String location, Long userId, Long itemId,
                                                    String generatedDesc, String originDesc, String status,
                                                    String aiCategory) {

        BizItemAiResult lastResult = aiResultDao.selectLatestByItemId(itemId);
        int newVersion = (lastResult == null ? 1 : lastResult.getResultVersion() + 1);
        // 1. 插入结果表
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
        // 2. 返回插入结果ID和版本号
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
            throw new BaseException("序列化 AI tags 失败");
        }
    }


    /**
     * 更新 item 的当前 AI 状态
     */
    @Override
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
