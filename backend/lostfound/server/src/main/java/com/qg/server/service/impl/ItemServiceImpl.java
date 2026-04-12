package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.common.constant.*;
import com.qg.common.context.BaseContext;
import com.qg.common.enums.BizItemStatusEnum;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.DeletionNotAllowedException;
import com.qg.common.exception.UpdateNotAllowedException;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.ItemPageQueryDTO;
import com.qg.pojo.dto.LostBizItemDTO;
import com.qg.pojo.dto.UpdateBizItemDTO;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizItemAiResult;
import com.qg.pojo.entity.BizItemImage;
import com.qg.pojo.vo.BizItemDetailVO;
import com.qg.pojo.vo.BizItemStatVO;
import com.qg.server.ai.util.LocationNormalizeUtil;
import com.qg.server.event.ItemAiGenerateEvent;
import com.qg.server.mapper.BizItemAiResultDao;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.mapper.BizItemImageDao;
import com.qg.server.mapper.BizItemAiTagDao;
import com.qg.server.service.ItemService;
import com.qg.pojo.entity.BizItemAiTag;
import com.qg.server.ai.client.ImageDescriptionClient;
import com.qg.server.ai.client.ImageDescriptionClient.ImageItem;
import com.qg.common.properties.AIProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 物品服务实现类
 * <p>
 * 支持：
 * 1. 发布/更新丢失/拾取物品
 * 2. AI多模态生成（文本/图片）并记录版本
 * 3. 多标签、多照片支持
 * 4. 详情页和分页搜索支持 AI 分类与标签
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<BizItemDao, BizItem> implements ItemService {

    private final BizItemImageDao bizItemImageDao;
    private final BizItemAiResultDao bizItemAiResultDao;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BizItemDao bizItemDao;
    private final BizItemAiTagDao bizItemAiTagDao;
     private  final ObjectMapper objectMapper;
    private final ImageDescriptionClient imageDescriptionClient;
    private final AIProperties aiProperties;

    // ===================== 发布/更新物品 =====================

    /**
     * 发布丢失物品
     */
    @Override
    @Transactional
    public void publishLostItem(LostBizItemDTO dto) {
        Long userId = BaseContext.getCurrentId();

        // 构造物品实体
        BizItem item = new BizItem();
        item.setUserId(userId);
        item.setType(BizItemTypeConstant.LOST);
        item.setStatus(BizItemStatusConstant.OPEN);
        item.setIsPinned(0);
        item.setAiStatus(BizItemAiResultStatusConstant.PENDING); // AI 处理状态
        item.setCurrentAiResultId(null);
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setLocation(dto.getLocation());
        item.setContactMethod(dto.getContactMethod());
        item.setHappenTime(dto.getHappenTime());
        item.setNormalizedLocation(LocationNormalizeUtil.normalize(dto.getLocation()));

        // 插入数据库
        bizItemDao.insert(item);

        // 保存图片列表
        saveItemImages(item.getId(), dto.getImageUrls());

        // 发布 AI 生成事件（异步执行）
        applicationEventPublisher.publishEvent(new ItemAiGenerateEvent(
                this,
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getNormalizedLocation(),
                userId,
                buildImageItems(dto.getImageUrls())
        ));

        // 清理缓存
        evictItemCaches(item.getId());
    }

    /**
     * 发布拾取物品（逻辑同丢失物品）
     */
    @Override
    @Transactional
    public void publishFoundItem(LostBizItemDTO dto) {
        Long userId = BaseContext.getCurrentId();
        BizItem item = new BizItem();
        BeanUtils.copyProperties(dto, item);
        item.setUserId(userId);
        item.setType(BizItemTypeConstant.FOUND);
        item.setStatus(BizItemStatusConstant.OPEN);
        item.setIsPinned(0);
        item.setAiStatus(BizItemAiResultStatusConstant.PENDING);
        item.setCurrentAiResultId(null);
        item.setNormalizedLocation(LocationNormalizeUtil.normalize(dto.getLocation()));
        item.setContactMethod(dto.getContactMethod());
        save(item);
        saveItemImages(item.getId(), dto.getImageUrls());

        applicationEventPublisher.publishEvent(new ItemAiGenerateEvent(
                this,
                item.getId(),
                dto.getTitle(),
                dto.getDescription(),
                item.getNormalizedLocation(),
                userId,
                buildImageItems(dto.getImageUrls())
        ));

        evictItemCaches(item.getId());
    }

    /**
     * 更新物品信息
     */
    @Override
    @Transactional
    public void updateItem(Long id, UpdateBizItemDTO dto) {
        Long userId = BaseContext.getCurrentId();
        BizItem oldItem = getById(id);
        if (oldItem == null) throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        if (!oldItem.getUserId().equals(userId)) throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);

        // 构造新的物品实体
        BizItem item = new BizItem();
        BeanUtils.copyProperties(dto, item);
        item.setId(id);
        item.setUserId(oldItem.getUserId());
        item.setType(oldItem.getType());
        item.setStatus(oldItem.getStatus());
        item.setIsPinned(oldItem.getIsPinned());
        item.setPinExpireTime(oldItem.getPinExpireTime());
        item.setContactMethod(dto.getContactMethod());
        // 清空旧 AI 结果
        item.setAiStatus(BizItemAiResultStatusConstant.PENDING);
        item.setCurrentAiResultId(null);

        updateById(item);

        // 删除旧图片并重新保存
        bizItemImageDao.delete(new LambdaQueryWrapper<BizItemImage>().eq(BizItemImage::getItemId, id));
        saveItemImages(id, dto.getImageUrls());

        // 发布 AI 生成事件
        applicationEventPublisher.publishEvent(new ItemAiGenerateEvent(
                this,
                id,
                dto.getTitle(),
                dto.getDescription(),
                item.getNormalizedLocation(),
                userId,
                buildImageItems(dto.getImageUrls())
        ));

        evictItemCaches(id);
    }

    // ===================== 查看详情 =====================

    /**
     * 获取物品详情
     * 支持：
     * 1. 多照片
     * 2. 最新 version 的 AI 分类和标签
     * 3. 缓存 30 分钟
     */
    public BizItemDetailVO getItem(Long id) {
        BizItem item = bizItemDao.selectById(id);
        if (item == null) throw new AbsentException("物品不存在");

        BizItemDetailVO vo = new BizItemDetailVO();
        BeanUtils.copyProperties(item, vo);

        // 图片
        List<String> images = bizItemImageDao.selectList(
                new LambdaQueryWrapper<BizItemImage>().eq(BizItemImage::getItemId, id)
        ).stream().map(BizItemImage::getUrl).toList();
        vo.setImageUrls(images);

        // 最新 version AI结果
        List<BizItemAiResult> aiResults = bizItemAiResultDao.selectByItemId(item.getId());
        int latestVersion = aiResults.stream().mapToInt(BizItemAiResult::getResultVersion).max().orElse(1);
        List<BizItemAiResult> latestResults = aiResults.stream()
                .filter(r -> r.getResultVersion() == latestVersion)
                .toList();

        String aiCategory = latestResults.stream()
                .map(BizItemAiResult::getAiCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));
        vo.setAiCategory(aiCategory);

        List<BizItemAiTag> tags = bizItemAiTagDao.selectByItemIdAndVersion(id, latestVersion);
        List<String> tagList = tags.stream()
                .flatMap(t -> {
                    try {
                        // 反序列化 JSON 字符串成 List<String>
                        return objectMapper.readValue(t.getAiTags(), new TypeReference<List<String>>() {}).stream();
                    } catch (Exception e) {
                        return Collections.<String>emptyList().stream();
                    }
                })
                .toList();
        vo.setAiTags(tagList);

        // AI描述也可以合并最新 version 的结果
        String aiDescription = latestResults.stream()
                .map(BizItemAiResult::getAiDescription)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
        vo.setAiDescription(aiDescription);

        vo.setAiStatus(item.getAiStatus());

        return vo;
    }


    // ===================== 分页搜索 =====================

    /**
     * 分页查询物品（公开列表）
     * 支持：
     * - title/description/aiCategory/tag 关键词搜索
     * - 分页缓存
     */
    @Override
    public PageResult<BizItemStatVO> pageList(ItemPageQueryDTO query) {
        Page<BizItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<BizItem> wrapper = new LambdaQueryWrapper<>();

        // 基础查询条件
        wrapper.eq(query.getType() != null, BizItem::getType, query.getType())
                .ge(query.getStartTime() != null, BizItem::getHappenTime, query.getStartTime())
                .le(query.getEndTime() != null, BizItem::getHappenTime, query.getEndTime())
                .eq(BizItem::getStatus, BizItemStatusConstant.OPEN)
                .orderByDesc(BizItem::getIsPinned)
                .orderByDesc(BizItem::getPinExpireTime)
                .orderByDesc(BizItem::getCreateTime);

        // -----------------关键词搜索-----------------
        boolean hasKeyword = query.getKeyword() != null && !query.getKeyword().trim().isEmpty();
        boolean hasLocation = query.getLocation() != null && !query.getLocation().trim().isEmpty();
        boolean hasAiCategory = query.getAiCategory() != null && !query.getAiCategory().trim().isEmpty();
        boolean hasCategory = query.getAiCategory() != null && !query.getAiCategory().trim().isEmpty(); // 新的 category 查询条件

        if (hasKeyword || hasLocation || hasAiCategory || hasCategory) {
            String kw = hasKeyword ? query.getKeyword().trim() : "";
            String loc = hasLocation ? query.getLocation().trim() : "";
            String aiCat = hasAiCategory ? query.getAiCategory().trim() : "";
            String category = hasCategory ? query.getAiCategory().trim() : ""; // 获取 aiCategory 参数

            wrapper.and(w -> {
                boolean hasPrevCondition = false;

                // 1. title + description 全文检索
                if (hasKeyword) {
                    w.apply("MATCH(title, description) AGAINST({0} IN NATURAL LANGUAGE MODE)", kw);
                    hasPrevCondition = true;
                }

                // 2. location 模糊匹配（改为 LIKE）
                if (hasLocation) {
                    String likePattern = "%" + loc + "%"; // 使用 LIKE 替代 REGEXP
                    if (hasPrevCondition) {
                        w.or();
                    }
                    w.like(BizItem::getLocation, likePattern); // 使用 LIKE 可以优化查询
                    hasPrevCondition = true;
                }

                // 3. ai_category 全文检索与 item_id 匹配（修正为从 biz_item_ai_result 获取 ai_category）
                if (hasKeyword || hasAiCategory) {
                    String aiSearchText = (kw + " " + aiCat).trim();
                    if (hasPrevCondition) {
                        w.or();
                    }
                    w.apply(
                            "JOIN biz_item_ai_result air ON air.item_id = biz_item.id " +
                                    "WHERE MATCH(air.ai_category) AGAINST({0} IN NATURAL LANGUAGE MODE)", aiSearchText
                    );
                }

                // 4. category 查询条件（如果有 category 参数，则加入筛选）
                if (hasCategory) {
                    if (hasPrevCondition) {
                        w.or();
                    }
                    String likeCategory = "%" + category + "%"; // 使用 LIKE 进行模糊匹配
                    w.apply("JOIN biz_item_ai_result air ON air.item_id = biz_item.id " +
                            "WHERE air.ai_category LIKE {0} ESCAPE '\\\\'", likeCategory); // 在 biz_item_ai_result 表中匹配 ai_category 字段
                }
            });
        }

        // 执行分页
        page(page, wrapper);

        return buildPageResult(page);
    }


    @Override
    public PageResult<BizItemStatVO> myPageList(ItemPageQueryDTO query) {
        Long userId = BaseContext.getCurrentId();
        query.setPageNum(query.getPageNum() != null ? query.getPageNum() : 1);
        query.setPageSize(query.getPageSize() != null ? query.getPageSize() : 10);

        Page<BizItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<BizItem> wrapper = new LambdaQueryWrapper<>();

        // 基础查询条件
        wrapper.eq(BizItem::getUserId, userId)
                .eq(query.getType() != null, BizItem::getType, query.getType())
                .like(query.getLocation() != null && !query.getLocation().trim().isEmpty(),
                        BizItem::getLocation, query.getLocation().trim())
                .ge(query.getStartTime() != null, BizItem::getHappenTime, query.getStartTime())
                .le(query.getEndTime() != null, BizItem::getHappenTime, query.getEndTime());

        // 处理关键词搜索
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            String kw = query.getKeyword().trim();
            String likeKw = "%" + escapeLike(kw) + "%"; // 转义 LIKE 特殊字符

            // 关键词检索，避免使用IN子查询，使用JOIN替代
            wrapper.and(w -> w.like(BizItem::getTitle, kw)
                    .or().like(BizItem::getDescription, kw)
                    .or().apply(
                            "JOIN biz_item_ai_result air ON air.item_id = biz_item.id " +
                                    "WHERE air.ai_category LIKE {0} ESCAPE '\\\\'", likeKw
                    ));
        }

        // 处理 category 查询条件，使用 LIKE 进行模糊匹配
        if (query.getAiCategory() != null && !query.getAiCategory().trim().isEmpty()) {
            String category = "%" + query.getAiCategory().trim() + "%"; // 使用 LIKE 进行模糊匹配

            // 使用 JOIN 替代子查询，通过 category 字段进行模糊匹配
            wrapper.and(w -> w.apply(
                    "JOIN biz_item_ai_result air ON air.item_id = biz_item.id " +
                            "WHERE air.ai_category LIKE {0} ESCAPE '\\\\'", category
            ));
        }

        // 执行分页
        page(page, wrapper);

        return buildPageResult(page);
    }
    /**
     * 转义 LIKE 特殊字符
     */
    private String escapeLike(String input) {
        // 如果输入为空，则返回空字符串
        if (input == null) {
            return "";
        }
        // 替换 % 和 _ 为转义字符
        return input.replaceAll("([\\\\%_])", "\\\\$1");
    }

    /**
     * 统一构造分页返回结果
     * 保持返回结构和字段内容不变
     */
    private PageResult<BizItemStatVO> buildPageResult(Page<BizItem> page) {
        List<BizItem> records = page.getRecords();
        if (records == null || records.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), page.getTotal(),
                    (int) page.getCurrent(), (int) page.getSize());
        }

        // 批量查询当前页所有 item 的 AI 结果，避免 N+1
        List<Long> itemIds = records.stream()
                .map(BizItem::getId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, String> itemIdToAiCategory = buildLatestAiCategoryMap(itemIds);

        List<BizItemStatVO> voList = records.stream().map(item -> {
            BizItemStatVO vo = new BizItemStatVO();
            BeanUtils.copyProperties(item, vo);
            vo.setStatusDesc(BizItemStatusEnum.getDescByCode(item.getStatus()));
            vo.setDescription(item.getDescription());
            vo.setAiCategory(itemIdToAiCategory.getOrDefault(item.getId(), "未知"));
            return vo;
        }).toList();

        return new PageResult<>(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 批量构造 itemId -> 最新 aiCategory 映射
     */
    private Map<Long, String> buildLatestAiCategoryMap(List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 批量查询 AI 结果，避免多次查询
        List<BizItemAiResult> aiResults = bizItemAiResultDao.selectByItemIds(itemIds);
        if (aiResults == null || aiResults.isEmpty()) {
            return Collections.emptyMap();
        }

        // 使用流处理和分组来减少冗余查询
        return aiResults.stream()
                .collect(Collectors.groupingBy(
                        BizItemAiResult::getItemId,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparingInt(BizItemAiResult::getResultVersion)),
                                aiResult -> aiResult.map(BizItemAiResult::getAiCategory).orElse("未知")
                        )
                ));
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id) {
        Long userId = BaseContext.getCurrentId();
        BizItem item = getById(id);
        if (item == null) throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        if (!item.getUserId().equals(userId)) throw new DeletionNotAllowedException(MessageConstant.DELETE_NOT_ALLOWED);

        removeById(id);
        evictItemCaches(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeItem(Long id) {
        Long userId = BaseContext.getCurrentId();
        BizItem item = getById(id);
        if (item == null) throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        if (!item.getUserId().equals(userId)) throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);

        if (!BizItemStatusConstant.CLOSED.equals(item.getStatus())) {
            item.setStatus(BizItemStatusConstant.CLOSED);
            updateById(item);
            evictItemCaches(id);
        }else{
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        }
    }

    @Override
    public void clearExpiredPinnedItems() {
        log.info("清除过期的置顶物品");
        List<BizItem> expiredPinnedItems = list(new LambdaQueryWrapper<BizItem>()
                .eq(BizItem::getIsPinned, 1)
                .ge(BizItem::getPinExpireTime, LocalDateTime.now()));
        expiredPinnedItems.forEach(item -> {
            item.setPinExpireTime(null);
            item.setIsPinned(0);
            updateById(item);
            evictItemCaches(item.getId());
        });
        log.info("清除完成，共清除{}个物品", expiredPinnedItems.size());
    }

    // ===================== 工具方法 =====================

//    private PageResult<BizItemStatVO> convertToVOPage(Page<BizItem> page) {
//        List<BizItemStatVO> list = page.getRecords().stream().map(item -> {
//            BizItemStatVO vo = new BizItemStatVO();
//            BeanUtils.copyProperties(item, vo);
//            vo.setStatus(BizItemStatusEnum.getDescByCode(item.getStatus()));
//            vo.setDescription(item.getDescription());
//            return vo;
//        }).toList();
//        return new PageResult<>(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
//    }

    private void saveItemImages(Long itemId, List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        for (String u : urls) {
            BizItemImage image = new BizItemImage();
            image.setItemId(itemId);
            image.setUrl(u);
            image.setCreateTime(LocalDateTime.now());
            bizItemImageDao.insert(image);
        }
    }

    private List<ImageItem> buildImageItems(List<String> urls) {
        if (urls == null) return Collections.emptyList();
        return urls.stream().map(u -> new ImageItem(u, "主图")).toList();
    }

    private void evictItemCaches(Long itemId) {
        redisTemplate.delete("ITEM_DETAIL_KEY:" + itemId);
        Set<String> keys = redisTemplate.keys("ITEM_PAGE_KEY:*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
    }
}
