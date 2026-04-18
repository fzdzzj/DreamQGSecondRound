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
import com.qg.common.exception.UpdateNotAllowedException;
import com.qg.common.result.PageResult;
import com.qg.common.util.SensitiveWordFilterUtil;
import com.qg.pojo.dto.ItemPageQueryDTO;
import com.qg.pojo.dto.LostBizItemDTO;
import com.qg.pojo.dto.UpdateBizItemDTO;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizItemAiResult;
import com.qg.pojo.entity.BizItemAiTag;
import com.qg.pojo.entity.BizItemImage;
import com.qg.pojo.vo.BizItemDetailVO;
import com.qg.pojo.vo.BizItemStatVO;
import com.qg.server.ai.client.ImageDescriptionClient.ImageItem;
import com.qg.server.event.ItemAiGenerateEvent;
import com.qg.server.mapper.BizItemAiResultDao;
import com.qg.server.mapper.BizItemAiTagDao;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.mapper.BizItemImageDao;
import com.qg.server.service.ItemService;
import com.qg.server.service.RiskMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private final BizItemDao bizItemDao;
    private final BizItemAiTagDao bizItemAiTagDao;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;
    private final RiskMonitorService riskMonitorService;
    private final SensitiveWordFilterUtil sensitiveWordFilterUtil;
    private final ApplicationContext applicationContext;

    private ItemServiceImpl getSelf() {
        return applicationContext.getBean(ItemServiceImpl.class);
    }

    /**
     * 发布丢失物品
     *
     * @param dto 物品详情DTO
     *            1. 构造物品实体（无事务）
     *            2. 核心事务只做DB操作
     *            3. 非DB操作全部移出事务（安全、不锁连接）
     *            4. 清理缓存
     *            5. 发布 AI 生成事件（异步）
     *            6. 风险检测
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishLostItem(LostBizItemDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("发布丢失物品,userId: {}", userId);
        // 1. 构造物品实体（无事务）
        BizItem item = buildItem(dto);
        item.setUserId(userId);
        item.setType(BizItemTypeConstant.LOST);
        // 2. 核心事务只做DB操作
        // 3. 非DB操作全部移出事务（安全、不锁连接）
        getSelf().saveItemAndImages(item, dto.getImageUrls());

        log.info("发布物品成功,itemId: {}", item.getId());
        // 4. 清理缓存
        evictItemCaches(item.getId());
        // 5. 发布 AI 生成事件（异步）
        applicationEventPublisher.publishEvent(new ItemAiGenerateEvent(
                this,
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getLocation(),
                userId,
                buildImageItems(dto.getImageUrls())
        ));

        // 6. 风险检测
        log.info("发布物品风险检测,itemId: {}", item.getId());
        riskMonitorService.onItemPublished(item);
    }

    /**
     * 发布拾取物品（逻辑同丢失物品,仅少一个风险检测）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishFoundItem(LostBizItemDTO dto) {
        Long userId = BaseContext.getCurrentId();
        // 1. 构造物品实体（无事务）
        log.info("发布拾取物品,userId: {}", userId);
        BizItem item = buildItem(dto);
        item.setUserId(userId);
        item.setType(BizItemTypeConstant.FOUND);
        // 2. 核心事务只做DB操作
        getSelf().saveItemAndImages(item, dto.getImageUrls());
        log.info("发布物品成功,itemId: {}", item.getId());
        // 3. 清理缓存
        evictItemCaches(item.getId());
        // 4. 发布 AI 生成事件（异步）
        applicationEventPublisher.publishEvent(new ItemAiGenerateEvent(
                this,
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getLocation(),
                userId,
                buildImageItems(dto.getImageUrls())
        ));
    }

    /**
     * 更新物品信息（外层无事务）
     * 1. 构造物品实体（无事务）
     * 2. 核心事务只做DB操作
     * 3. 非DB操作全部移出事务（安全规范）
     * 4. 发布 AI 生成事件（异步）
     * 5. 清理缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(Long id, UpdateBizItemDTO dto) {
        Long userId = BaseContext.getCurrentId();

        // 1. 查询、校验、构造（无事务）
        BizItem oldItem = getById(id);
        if (oldItem == null) {
            log.warn("物品不存在,itemId: {},用户ID: {}", id, userId);
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }
        if (!oldItem.getUserId().equals(userId)) {
            log.warn("物品发布者不是当前用户,itemId: {},用户ID: {}", id, userId);
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        }

        BizItem item = buildUpdatedItem(oldItem, dto);
        log.info("更新物品,itemId: {}", id);
        // 2. 【核心事务：只做DB】
        getSelf().updateItemDb(id, item, dto.getImageUrls());

        // 3. 非DB操作全部移出事务（安全规范）
        if (item.getType().equals(BizItemTypeConstant.LOST) && (item.getStatus().equals(BizItemStatusConstant.CLOSED) || item.getStatus().equals(BizItemStatusConstant.DELETED) || item.getStatus().equals(BizItemStatusConstant.MATCHED))) {
            riskMonitorService.onItemFound(item);
        }
        // 4. 清理缓存
        evictItemCaches(id);

        // 5. 发布 AI 生成事件（异步）
        applicationEventPublisher.publishEvent(new ItemAiGenerateEvent(
                this,
                id,
                dto.getTitle(),
                dto.getDescription(),
                item.getLocation(),
                userId,
                buildImageItems(dto.getImageUrls())
        ));

    }

    // ===================== 查看详情 =====================

    /**
     * 获取物品详情
     * 支持：
     * 1. 多照片
     * 2. 最新 version 的 AI 分类和标签
     * 3. 缓存 30 分钟
     * <p>
     * 缓存逻辑：
     * 1. 从缓存中获取
     * 1.1 如果是物品发布者，直接返回缓存数据
     * 1.2 如果不是物品发布者，对缓存数据进行敏感词过滤后返回
     * 2. 缓存不存在，从 DB 中获取
     * 2.1 如果是物品发布者，直接返回 DB 数据
     * 2.2 如果不是物品发布者，对 DB 数据进行敏感词过滤后返回
     * 3. 获取图片URL
     * 4. 获取最新 version AI结果
     * 5. AI分类
     * 6. AI标签
     * 7.AI描述
     * 8. 缓存结果
     */
    public BizItemDetailVO getItem(Long id) {
        String cacheKey = RedisConstant.ITEM_DETAIL_KEY + id;
        // 1. 从缓存中获取
        BizItemDetailVO cached = (BizItemDetailVO) redisTemplate.opsForValue().get(cacheKey);
        BizItem item = getById(id);
        if (item == null) {
            log.warn("物品不存在,itemId: {}", id);
            throw new AbsentException("物品不存在");
        }
        Long ownerId = item.getUserId();

        if (cached != null) {
            log.info("从缓存中获取物品详情,itemId: {}", id);
            if (!ownerId.equals(BaseContext.getCurrentId())) {
                cached.setDescription(sensitiveWordFilterUtil.filter(cached.getDescription()));
                cached.setAiDescription(sensitiveWordFilterUtil.filter(cached.getAiDescription()));
            }
            return cached;
        }
        // 2. 缓存不存在，从 DB 中获取
        BizItemDetailVO vo = new BizItemDetailVO();
        BeanUtils.copyProperties(item, vo);
        vo.setContactUserId(item.getUserId());
        vo.setContactMethod(item.getContactMethod());
        // 3. 敏感词过滤
        if (ownerId.equals(BaseContext.getCurrentId())) {
            log.info("从数据库中获取物品详情,itemId: {}", id);
            vo.setDescription(sensitiveWordFilterUtil.filter(item.getDescription()));
        }

        // 4. 图片URL
        List<String> images = bizItemImageDao.selectList(
                new LambdaQueryWrapper<BizItemImage>().eq(BizItemImage::getItemId, id)
        ).stream().map(BizItemImage::getUrl).toList();
        vo.setImageUrls(images);
        log.info("从数据库中获取物品图片,size: {},itemId: {}", id, images.size());

        // 5. 最新 version AI结果
        List<BizItemAiResult> aiResults = bizItemAiResultDao.selectByItemId(item.getId());
        int latestVersion = aiResults.stream().mapToInt(BizItemAiResult::getResultVersion).max().orElse(1);
        List<BizItemAiResult> latestResults = aiResults.stream()
                .filter(r -> r.getResultVersion() == latestVersion)
                .toList();
        log.info("从数据库中获取物品最新 version AI结果,itemId: {},version: {},size: {}", id, latestVersion, latestResults.size());
        // 6. AI分类
        String aiCategory = latestResults.stream()
                .map(BizItemAiResult::getAiCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));
        vo.setAiCategory(aiCategory);
        log.info("从数据库中获取物品最新 version AI分类,itemId: {},category: {}", id, aiCategory);
        // 7. AI标签
        List<BizItemAiTag> tags = bizItemAiTagDao.selectByItemIdAndVersion(id, latestVersion);
        List<String> tagList = tags.stream()
                .flatMap(t -> {
                    try {
                        // 反序列化 JSON 字符串成 List<String>
                        return objectMapper.readValue(t.getAiTags(), new TypeReference<List<String>>() {
                        }).stream();
                    } catch (Exception e) {
                        return Collections.<String>emptyList().stream();
                    }
                })
                .toList();
        vo.setAiTags(tagList);
        log.info("从数据库中获取物品最新 version AI标签,itemId: {},size: {}", id, tagList.size());

        // 8. AI描述
        String aiDescription = latestResults.stream()
                .map(BizItemAiResult::getAiDescription)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
        if (!BaseContext.getCurrentId().equals(ownerId)) {
            vo.setAiDescription(sensitiveWordFilterUtil.filter(aiDescription));
            log.info("从数据库中获取物品最新 version AI描述,itemId: {},description: {}", id, aiDescription);
        }
        vo.setAiStatus(item.getAiStatus());
        log.info("从数据库中获取物品最新 version AI状态,itemId: {},status: {}", id, item.getAiStatus());
        // 9.写入 Redis 缓存，过期 30 分钟
        redisTemplate.opsForValue().set(cacheKey, vo, 30, java.util.concurrent.TimeUnit.MINUTES);
        return vo;
    }


    // ===================== 分页搜索 =====================

    /**
     * 分页查询物品（公开列表）
     * 支持：
     * - title/description/aiCategory/tag 关键词搜索
     * - 分页缓存
     * <p>
     * 1. 从缓存中获取
     * 2. 从数据库中查询
     * 3. 缓存结果
     */
    @Override
    public PageResult<BizItemStatVO> pageList(ItemPageQueryDTO query) {
        // 时间格式化工具
        // 时间格式化（固定格式，保证时间不同 Key 一定不同）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String cacheKey = String.format(RedisConstant.ITEM_PAGE_KEY_FORMAT,
                query.getPageNum(),
                query.getPageSize(),
                query.getType() == null ? "all" : query.getType(),
                query.getKeyword() == null ? "" : query.getKeyword().trim(),
                query.getLocation() == null ? "" : query.getLocation().trim(),
                query.getAiCategory() == null ? "" : query.getAiCategory().trim(),
                query.getStartTime() == null ? "" : query.getStartTime().format(formatter),
                query.getEndTime() == null ? "" : query.getEndTime().format(formatter)
        );
        log.info("从缓存中获取物品分页结果,key: {}", cacheKey);
        // 1.尝试从缓存获取
        PageResult<BizItemStatVO> cached = (PageResult<BizItemStatVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("从缓存中获取物品分页结果,size: {}", cached.getList().size());
            return cached;
        }
        // 2. 从数据库中查询
        Page<BizItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<BizItem> wrapper = new LambdaQueryWrapper<>();

        // 1.1 基础查询条件
        wrapper.eq(query.getType() != null, BizItem::getType, query.getType())
                .ge(query.getStartTime() != null, BizItem::getHappenTime, query.getStartTime())
                .le(query.getEndTime() != null, BizItem::getHappenTime, query.getEndTime())
                .eq(BizItem::getStatus, BizItemStatusConstant.OPEN)
                .orderByDesc(BizItem::getIsPinned)
                .orderByDesc(BizItem::getPinExpireTime)
                .orderByDesc(BizItem::getHappenTime)
                .orderByDesc(BizItem::getCreateTime);

        // 2. 关键词查询
        // ================= 搜索条件 =================
        final String kw = (query.getKeyword() == null) ? "" : query.getKeyword().trim();
        final String loc = (query.getLocation() == null) ? "" : query.getLocation().trim();
        final String aiCat = (query.getAiCategory() == null) ? "" : query.getAiCategory().trim();

        boolean hasKeyword = !kw.isEmpty();
        boolean hasLocation = !loc.isEmpty();
        boolean hasAiCategory = !aiCat.isEmpty();

        if (hasKeyword || hasLocation || hasAiCategory) {
            wrapper.and(w -> {
                // 统一标记：是否已经有条件
                boolean hasAny = false;

                // 2.1. title + description 全文检索
                if (hasKeyword) {
                    log.info("从数据库中查询物品分页结果,keyword: {}", kw);
                    w.apply("MATCH(title, description) AGAINST({0} IN NATURAL LANGUAGE MODE)", kw);
                    hasAny = true;
                }

                // 2.2. location 模糊匹配
                if (hasLocation) {
                    if (hasAny) w.or(); // 前面有条件才加 OR
                    log.info("从数据库中查询物品分页结果,location: {}", loc);
                    w.like(BizItem::getLocation, "%" + loc + "%");
                    hasAny = true;
                }

                // 2.3. ai_category（exists + fulltext + like 合并）
                if (hasAiCategory) {
                    if (hasAny) w.or();
                    log.info("从数据库中查询物品分页结果,ai_category: {}", aiCat);

                    w.exists("""
                                SELECT 1 FROM biz_item_ai_result air
                                WHERE air.item_id = biz_item.id
                                  AND air.result_version = (SELECT MAX(result_version) FROM biz_item_ai_result WHERE item_id = air.item_id)
                                  AND air.ai_category IS NOT NULL AND air.ai_category != ''
                                  AND air.ai_category LIKE {0} ESCAPE '\\\\'
                                """,
                            "%" + aiCat + "%"
                    );
                }
            });
        }
        // 2.4. 执行查询
        page(page, wrapper);
        // 2.5. 构建分页结果
        PageResult<BizItemStatVO> voList = buildPageResult(page);
        log.info("从数据库中查询物品分页结果,size: {}", voList.getList().size());
        // 3.过期 30 分钟
        redisTemplate.opsForValue().set(cacheKey, voList, 30, java.util.concurrent.TimeUnit.MINUTES);
        return voList;
    }

    /**
     * 分页查询物品（个人列表）
     * 支持：
     * - title/description/aiCategory/tag 关键词搜索
     * <p>
     * 1.创建分页查询条件
     * 2.添加搜索条件
     * 3.执行查询
     * 4.构建分页结果
     */
    @Override
    public PageResult<BizItemStatVO> myPageList(ItemPageQueryDTO query) {

        Long userId = BaseContext.getCurrentId();
        log.error("分页查询物品开始,userId: {}", userId);
        //1.创建分页查询条件
        Page<BizItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<BizItem> wrapper = new LambdaQueryWrapper<>();
        log.info("pageSize: {}", query.getPageSize());
        // ================= 基础条件 =================
        wrapper.eq(BizItem::getUserId, userId)
                .eq(query.getType() != null, BizItem::getType, query.getType())
                .ge(query.getStartTime() != null, BizItem::getHappenTime, query.getStartTime())
                .le(query.getEndTime() != null, BizItem::getHappenTime, query.getEndTime())
                .orderByDesc(BizItem::getIsPinned)
                .orderByDesc(BizItem::getHappenTime)
                .orderByDesc(BizItem::getCreateTime);

        // ================= 搜索条件 =================
        final String kw = (query.getKeyword() == null) ? "" : query.getKeyword().trim();
        final String aiCat = (query.getAiCategory() == null) ? "" : query.getAiCategory().trim();

        boolean hasKeyword = !kw.isEmpty();
        boolean hasAiCategory = !aiCat.isEmpty();
        //2. 添加搜索条件
        // ================= keyword 搜索 =================
        if (hasKeyword) {
            log.info("从数据库中查询物品分页结果,keyword: {}", kw);
            String likeKw = "%" + escapeLike(kw) + "%";

            wrapper.and(w -> w.like(BizItem::getTitle, kw)
                    .or().like(BizItem::getDescription, kw)
                    .or().exists(
                            "SELECT 1 FROM biz_item_ai_result air " +
                                    "WHERE air.item_id = biz_item.id " +
                                    "AND air.ai_category LIKE {0} ESCAPE '\\\\'",
                            likeKw
                    ));
        }

        // ================= category 搜索 =================
        if (hasAiCategory) {
            log.info("从数据库中查询物品分页结果,ai_category: {}", aiCat);
            String categoryLike = "%" + aiCat + "%";

            wrapper.and(w -> w.exists(
                    "SELECT 1 FROM biz_item_ai_result air " +
                            "WHERE air.item_id = biz_item.id " +
                            "AND air.ai_category LIKE {0} ESCAPE '\\\\'",
                    categoryLike
            ));
        }
        // 3. 执行查询
        page(page, wrapper);
        // 4. 构建分页结果
        return buildPageResult(page);
    }

    /**
     * 删除物品
     * 1.检查物品是否存在
     * 2.检查物品是否属于当前用户
     * 3.删除物品
     * 4.清除缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        log.info("删除物品开始,itemId: {}", id);
        Long currentUserId = BaseContext.getCurrentId();
        String currentRole = BaseContext.getCurrentRole();

        // 1. 检查物品是否存在
        BizItem item = bizItemDao.selectById(id);
        if (item == null) {
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        // 2. 权限校验：只能删除自己的 或 管理员删除
        boolean isOwner = item.getUserId().equals(currentUserId);
        boolean isAdmin = RoleConstant.ADMIN.equals(currentRole);

        if (!isOwner && !isAdmin) {
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        }

        // 3. 执行软删除
        bizItemDao.deleteById(item);
        // 4. 清除缓存
        evictItemCaches(id);

        log.info("删除物品成功,itemId: {}", id);
    }


    /**
     * 关闭物品
     * 1.检查物品是否存在
     * 2.检查物品是否属于当前用户
     * 3.关闭物品
     * 4.清除缓存
     */
    @Override
    public void closeItem(Long id) {
        Long userId = BaseContext.getCurrentId();
        // 1. 检查物品是否存在
        log.info("关闭物品,itemId: {}", id);
        BizItem item = getById(id);
        if (item == null) {
            log.warn("物品不存在,itemId: {}", id);
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }
        if (!item.getUserId().equals(userId) || !BaseContext.getCurrentRole().equals(RoleConstant.ADMIN)) {
            log.warn("物品不属于当前用户，无法关闭,itemId: {}", item.getUserId());
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        }
        // 2. 检查物品是否已关闭

        if (!BizItemStatusConstant.CLOSED.equals(item.getStatus())) {
            if (item.getStatus().equals(BizItemStatusConstant.OPEN)) {
                log.info("物品已关闭，清理缓存数据,itemId: {}", id);
                evictItemCaches(id);
            }
            log.info("物品已关闭，更新状态为CLOSED,itemId: {}", id);
            item.setStatus(BizItemStatusConstant.CLOSED);
            updateById(item);
        } else {
            log.warn("物品已关闭，无需重复操作,itemId: {}", id);
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        }
    }

    /**
     * 清除过期的置顶物品
     * 1.查询所有过期的置顶物品
     * 2.更新物品状态为非置顶
     * 3.清除缓存
     */
    @Override
    public void clearExpiredPinnedItems() {
        log.info("清除过期的置顶物品");
        // 1. 查询所有过期的置顶物品
        List<BizItem> expiredPinnedItems = list(new LambdaQueryWrapper<BizItem>()
                .eq(BizItem::getIsPinned, 1)
                .lt(BizItem::getPinExpireTime, LocalDateTime.now()));
        // 2. 更新物品状态为非置顶
        expiredPinnedItems.forEach(item -> {
            item.setPinExpireTime(null);
            item.setIsPinned(0);
            updateById(item);
            evictItemCaches(item.getId());
        });
        log.info("清除完成，共清除{}个物品", expiredPinnedItems.size());
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
     * <p>
     * 1. 批量查询当前页所有 item 的 AI 结果，避免 N+1
     * 2.映射Mapper，将item最新最新的AI结果映射到VO中
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
            vo.setDescription(sensitiveWordFilterUtil.filter(item.getDescription()));
            vo.setAiCategory(itemIdToAiCategory.getOrDefault(item.getId(), "未知"));
            return vo;
        }).toList();

        return new PageResult<>(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 批量构造 itemId -> 最新 aiCategory 映射
     * 1. 批量查询 AI 结果，避免多次查询
     * 2.使用流处理和分组来减少冗余查询
     */
    private Map<Long, String> buildLatestAiCategoryMap(List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        //1. 批量查询 AI 结果，避免多次查询
        List<BizItemAiResult> aiResults = bizItemAiResultDao.selectByItemIds(itemIds);
        if (aiResults == null || aiResults.isEmpty()) {
            return Collections.emptyMap();
        }

        // 2.使用流处理和分组来减少冗余查询
        return aiResults.stream()
                .collect(Collectors.groupingBy(
                        BizItemAiResult::getItemId,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparingInt(BizItemAiResult::getResultVersion)),
                                aiResult -> aiResult.map(BizItemAiResult::getAiCategory).orElse("未知")
                        )
                ));
    }


    /**
     * 只包含：插入物品 + 插入图片
     * 原子性，事务极快，无任何慢操作
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveItemAndImages(BizItem item, List<String> imageUrls) {
        // 插入物品
        bizItemDao.insert(item);
        // 插入图片
        saveItemImages(item.getId(), imageUrls);
    }

    /**
     * 批量插入图片
     */
    @Override
    public void saveItemImages(Long itemId, List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        for (String u : urls) {
            BizItemImage image = new BizItemImage();
            image.setItemId(itemId);
            image.setUrl(u);
            image.setCreateTime(LocalDateTime.now());
            bizItemImageDao.insert(image);
        }
        log.info("插入物品{}的{}张图片", itemId, urls.size());
    }

    /**
     * 批量构造图片Item，用于AI
     */
    private List<ImageItem> buildImageItems(List<String> urls) {
        if (urls == null) return Collections.emptyList();
        return urls.stream().map(u -> new ImageItem(u, "主图")).toList();
    }

    /**
     * 构建物品
     */
    private BizItem buildItem(LostBizItemDTO dto) {
        BizItem item = new BizItem();
        item.setStatus(BizItemStatusConstant.OPEN);
        item.setIsPinned(0);
        item.setAiStatus(BizItemAiResultStatusConstant.PENDING);
        item.setCurrentAiResultId(null);
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setLocation(dto.getLocation());
        item.setContactMethod(dto.getContactMethod());
        item.setHappenTime(dto.getHappenTime());
        item.setAiStatus(BizItemAiResultStatusConstant.PENDING);
        item.setCurrentAiResultId(null);
        return item;
    }

    /**
     * 构建更新后的物品
     */
    private BizItem buildUpdatedItem(BizItem oldItem, UpdateBizItemDTO dto) {
        BizItem item = new BizItem();
        BeanUtils.copyProperties(dto, item);
        log.info("更新物品状态：{}", dto.getStatus());
        item.setId(oldItem.getId());
        item.setUserId(oldItem.getUserId());
        item.setType(oldItem.getType());
        item.setStatus(dto.getStatus());
        item.setIsPinned(oldItem.getIsPinned());
        item.setPinExpireTime(oldItem.getPinExpireTime());
        item.setContactMethod(dto.getContactMethod());
        return item;
    }

    /**
     * 更新物品数据库
     * 原子性，事务极快，无任何慢操作
     * 1.更新物品
     * 2.删除旧图
     * 3.保存新图
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateItemDb(Long id, BizItem item, List<String> imageUrls) {
        // 1.更新物品
        updateById(item);

        // 2.删除旧图
        bizItemImageDao.delete(new LambdaQueryWrapper<BizItemImage>().eq(BizItemImage::getItemId, id));

        // 3.保存新图
        saveItemImages(id, imageUrls);
    }

    /**
     * 删除物品缓存，包括详情页和分页缓存
     */
    private void evictItemCaches(Long itemId) {
        redisTemplate.delete(RedisConstant.ITEM_DETAIL_KEY + itemId);
        Set<String> keys = redisTemplate.keys(RedisConstant.ITEM_PAGE_KEY + "*");
        if (keys != null && !keys.isEmpty()) redisTemplate.delete(keys);
    }
}
