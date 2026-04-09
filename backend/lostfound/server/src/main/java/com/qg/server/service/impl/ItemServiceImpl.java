package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qg.common.constant.BizItemStatus;
import com.qg.common.constant.BizItemType;
import com.qg.common.constant.ItemTriggerType;
import com.qg.common.constant.MessageConstant;
import com.qg.common.context.BaseContext;
import com.qg.common.enums.BizItemStatusEnum;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.DeletionNotAllowedException;
import com.qg.common.exception.UpdateNotAllowedException;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.LostBizItemDTO;
import com.qg.pojo.dto.UpdateBizItemDTO;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizItemAiResult;
import com.qg.pojo.entity.BizItemImage;
import com.qg.pojo.dto.ItemPageQueryDTO;
import com.qg.pojo.vo.BizItemStatVO;
import com.qg.pojo.vo.BizItemDetailVO;
import com.qg.server.event.ItemAiGenerateEvent;
import com.qg.server.mapper.BizItemAiResultDao;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.mapper.BizItemImageDao;
import com.qg.server.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<BizItemDao, BizItem> implements ItemService {

    private final BizItemImageDao bizItemImageDao;
    private final BizItemAiResultDao bizItemAiResultDao;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;
    /**
     * 详情缓存 key 前缀
     * 示例：item:detail:1001
     */
    private static final String ITEM_DETAIL_KEY = "item:detail:";

    /**
     * 分页缓存 key 前缀
     * 示例：item:page:type=LOST:keyword=校园卡:location=图书馆:page=1:size=10
     */
    private static final String ITEM_PAGE_KEY = "item:page:";


    /**
     * 发布丢失物品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishLostItem(LostBizItemDTO lostBizItemDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("发布丢失物品开始，userId={}", userId);

        BizItem bizItem = new BizItem();
        BeanUtils.copyProperties(lostBizItemDTO, bizItem);
        bizItem.setUserId(userId);
        bizItem.setType(BizItemType.LOST);
        bizItem.setStatus(BizItemStatus.OPEN);
        bizItem.setIsPinned(0);
        // 新发布后等待 AI 分析
        bizItem.setAiStatus("PENDING");
        bizItem.setAiCategory(null);
        bizItem.setAiTags(null);
        bizItem.setCurrentAiResultId(null);

        save(bizItem);  // 使用 IService 提供的 save 方法
        saveItemImages(bizItem.getId(), lostBizItemDTO.getImageUrls());  // 保存物品图片
        applicationEventPublisher.publishEvent(
                new ItemAiGenerateEvent(this,bizItem.getId(), ItemTriggerType.CREATE)
        );
        // 新增后清理首页热点缓存
        evictItemCaches(bizItem.getId());

        log.info("发布丢失物品成功，itemId={}, userId={}", bizItem.getId(), userId);
    }

    /**
     * 发布拾取物品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishFoundItem(LostBizItemDTO lostBizItemDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("发布拾取物品开始，userId={}", userId);

        BizItem bizItem = new BizItem();
        BeanUtils.copyProperties(lostBizItemDTO, bizItem);
        bizItem.setUserId(userId);
        bizItem.setType(BizItemType.FOUND);
        bizItem.setStatus(BizItemStatus.OPEN);
        bizItem.setIsPinned(0);
        // 新发布后等待 AI 分析
        bizItem.setAiStatus("PENDING");
        bizItem.setAiCategory(null);
        bizItem.setAiTags(null);
        bizItem.setCurrentAiResultId(null);
        save(bizItem);  // 使用 IService 提供的 save 方法
        saveItemImages(bizItem.getId(), lostBizItemDTO.getImageUrls());  // 保存物品图片
        applicationEventPublisher.publishEvent(
                new ItemAiGenerateEvent(this,bizItem.getId(), ItemTriggerType.CREATE)
        );
        // 新增后清理首页热点缓存
        evictItemCaches(bizItem.getId());

        log.info("发布拾取物品成功，itemId={}, userId={}", bizItem.getId(), userId);
    }

    /**
     * 修改物品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(Long id, UpdateBizItemDTO updateBizItemDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("修改物品开始，itemId={}, userId={}", id, userId);

        BizItem oldItem = getById(id);  // 使用 IService 提供的 getById 方法
        if (oldItem == null) {
            log.warn("修改物品失败，物品不存在，itemId={}", id);
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        // 数据级权限校验：只能改自己的物品
        if (!oldItem.getUserId().equals(userId)) {
            log.warn("修改物品失败，无权修改他人物品，itemId={}, userId={}", id, userId);
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        }

        // 举报中或已关闭的数据，不允许继续修改
        if (BizItemStatus.REPORTED.equals(oldItem.getStatus())
                || BizItemStatus.CLOSED.equals(oldItem.getStatus())) {
            log.warn("修改物品失败，当前状态不允许修改，itemId={}, status={}", id, oldItem.getStatus());
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        }

        // 构建更新对象：保留不允许被前端直接覆盖的字段
        BizItem bizItem = new BizItem();
        BeanUtils.copyProperties(updateBizItemDTO, bizItem);
        bizItem.setId(id);
        bizItem.setUserId(oldItem.getUserId());
        bizItem.setType(oldItem.getType());
        bizItem.setStatus(oldItem.getStatus());
        bizItem.setIsPinned(oldItem.getIsPinned());
        bizItem.setPinExpireTime(oldItem.getPinExpireTime());
        bizItem.setAiCategory(oldItem.getAiCategory());
        bizItem.setAiTags(oldItem.getAiTags());

        // 用户修改了物品核心信息后，旧 AI 结果视为失效
        bizItem.setAiStatus("PENDING");
        bizItem.setAiCategory(null);
        bizItem.setAiTags(null);
        bizItem.setCurrentAiResultId(null);

        updateById(bizItem);  // 使用 IService 提供的 updateById 方法

        // 图片采用全量替换策略：先删旧图，再插新图
        bizItemImageDao.delete(
                new LambdaQueryWrapper<BizItemImage>()
                        .eq(BizItemImage::getItemId, id)
        );
        saveItemImages(id, updateBizItemDTO.getImageUrls());  // 保存更新后的物品图片
        applicationEventPublisher.publishEvent(
                new ItemAiGenerateEvent(this,bizItem.getId(), ItemTriggerType.CREATE)
        );
        // 修改后清理缓存
        evictItemCaches(id);

        log.info("修改物品成功，itemId={}, userId={}", id, userId);
    }

    /**
     * 获取物品详情
     */
    @Override
    public BizItemDetailVO getItem(Long id) {
        String cacheKey = ITEM_DETAIL_KEY + id;

        // 1. 先查缓存
        Object cache = redisTemplate.opsForValue().get(cacheKey);
        if (cache instanceof BizItemDetailVO bizItemDetailVO) {
            log.info("命中物品详情缓存，itemId={}", id);
            return bizItemDetailVO;
        }

        // 2. 查数据库
        BizItem bizItem = getById(id);  // 使用 IService 提供的 getById 方法
        if (bizItem == null) {
            log.warn("获取物品详情失败，物品不存在，itemId={}", id);
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        if (BizItemStatus.REPORTED.equals(bizItem.getStatus())) {
            log.warn("获取物品详情失败，物品不可见，itemId={}, status={}", id, bizItem.getStatus());
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        // 关闭状态仅允许发布者本人查看
        if (BizItemStatus.CLOSED.equals(bizItem.getStatus())) {
            Long currentUserId = BaseContext.getCurrentId();
            if (currentUserId == null || !bizItem.getUserId().equals(currentUserId)) {
                log.warn("获取物品详情失败，关闭状态仅本人可见，itemId={}", id);
                throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
            }
        }

        // 3. 组装 VO
        BizItemDetailVO bizItemDetailVO = new BizItemDetailVO();
        BeanUtils.copyProperties(bizItem, bizItemDetailVO);

        List<String> imageUrls = bizItemImageDao.selectList(
                new LambdaQueryWrapper<BizItemImage>()
                        .eq(BizItemImage::getItemId, id)
                        .orderByAsc(BizItemImage::getId)
        ).stream().map(BizItemImage::getUrl).toList();

        bizItemDetailVO.setImageUrls(imageUrls);
        bizItemDetailVO.setStatusDesc(BizItemStatusEnum.getDescByCode(bizItem.getStatus()));

        // 详情页按需查询当前生效 AI 结果
        if (bizItem.getCurrentAiResultId() != null) {
            BizItemAiResult aiResult = bizItemAiResultDao.selectOne(
                    new LambdaQueryWrapper<BizItemAiResult>()
                            .eq(BizItemAiResult::getId, bizItem.getCurrentAiResultId())
                            .eq(BizItemAiResult::getItemId, id)
                            .eq(BizItemAiResult::getIsDeleted, 0)
                            .last("limit 1")
            );
            if (aiResult != null) {
                bizItemDetailVO.setAiDescription(aiResult.getAiDescription());

                // 兜底：如果主表摘要字段为空，可回填展示
                if (StringUtils.isBlank(bizItemDetailVO.getAiCategory())) {
                    bizItemDetailVO.setAiCategory(aiResult.getAiCategory());
                }
                if (StringUtils.isBlank(bizItemDetailVO.getAiTags())) {
                    bizItemDetailVO.setAiTags(aiResult.getAiTags());
                }
            }
        }
        // 4. 回写缓存
        redisTemplate.opsForValue().set(cacheKey, bizItemDetailVO, 30, TimeUnit.MINUTES);
        log.info("写入物品详情缓存，itemId={}", id);

        return bizItemDetailVO;
    }

    /**
     * 删除物品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("删除物品开始，itemId={}, userId={}", id, userId);

        BizItem bizItem = getById(id);  // 使用 IService 提供的 getById 方法
        if (bizItem == null) {
            log.warn("删除物品失败，物品不存在，itemId={}", id);
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        if (!bizItem.getUserId().equals(userId)) {
            log.warn("删除物品失败，无权删除他人物品，itemId={}, userId={}", id, userId);
            throw new DeletionNotAllowedException(MessageConstant.DELETE_NOT_ALLOWED);
        }

        removeById(id);  // 使用 IService 提供的 removeById 方法
        evictItemCaches(id);  // 清理缓存

        log.info("删除物品成功，itemId={}, userId={}", id, userId);
    }

    /**
     * 关闭物品（仅发布者可以关闭）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeItem(Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("关闭物品开始，itemId={}, userId={}", id, userId);

        BizItem bizItem = getById(id);  // 使用 IService 提供的 getById 方法
        if (bizItem == null) {
            log.warn("关闭物品失败，物品不存在，itemId={}", id);
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        if (!bizItem.getUserId().equals(userId)) {
            log.warn("关闭物品失败，无权关闭他人物品，itemId={}, userId={}", id, userId);
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        }

        // 物品状态不为 OPEN 时不可关闭
        if (BizItemStatus.CLOSED.equals(bizItem.getStatus())) {
            log.warn("关闭物品失败，物品已是关闭状态，itemId={}", id);
            return;
        }

        bizItem.setStatus(BizItemStatus.CLOSED);
        updateById(bizItem);  // 使用 IService 提供的 updateById 方法

        // 关闭后清理缓存
        evictItemCaches(id);

        log.info("关闭物品成功，itemId={}, userId={}", id, userId);
    }
    /**
     * 分页查询物品列表
     */
    @Override
    public PageResult<BizItemStatVO> pageList(ItemPageQueryDTO query) {

        boolean cacheable = query.getPageNum() != null
                && query.getPageNum() == 1
                && query.getPageSize() != null
                && query.getPageSize() <= 10;

        String cacheKey = null;
        if (cacheable) {
            cacheKey = buildPageCacheKey(query);
            Object cache = redisTemplate.opsForValue().get(cacheKey);
            if (cache instanceof PageResult<?> pageResult) {
                @SuppressWarnings("unchecked")
                PageResult<BizItemStatVO> result = (PageResult<BizItemStatVO>) pageResult;
                log.info("命中物品分页缓存，key={}", cacheKey);
                return result;
            }
        }

        Page<BizItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<BizItem> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(StringUtils.isNotBlank(query.getType()), BizItem::getType, query.getType())
                .like(StringUtils.isNotBlank(query.getLocation()), BizItem::getLocation, query.getLocation())
                .ge(query.getStartTime() != null, BizItem::getHappenTime, query.getStartTime())
                .le(query.getEndTime() != null, BizItem::getHappenTime, query.getEndTime())
                .eq(StringUtils.isNotBlank(query.getAiCategory()), BizItem::getAiCategory, query.getAiCategory())
                .eq(StringUtils.isNotBlank(query.getAiStatus()), BizItem::getAiStatus, query.getAiStatus())
                .eq(BizItem::getStatus, BizItemStatus.OPEN);

        // 关键词搜索：标题 or 描述
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(BizItem::getTitle, query.getKeyword())
                    .or()
                    .like(BizItem::getDescription, query.getKeyword()));
        }

        // 排序：置顶优先，再按发布时间倒序
        wrapper.orderByDesc(BizItem::getIsPinned)
                .orderByDesc(BizItem::getPinExpireTime)
                .orderByDesc(BizItem::getCreateTime);

        page(page, wrapper);

        PageResult<BizItemStatVO> result = convertToVOPage(page);

        if (cacheable && cacheKey != null) {
            redisTemplate.opsForValue().set(cacheKey, result, 5, TimeUnit.MINUTES);
            log.info("写入物品分页缓存，key={}", cacheKey);
        }

        return result;
    }

    /**
     * 获取当前用户发布的物品列表（分页）
     */
    @Override
    public PageResult<BizItemStatVO> myPageList(ItemPageQueryDTO query) {
        Long userId = BaseContext.getCurrentId();

        Page<BizItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<BizItem> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(BizItem::getUserId, userId)
                .eq(StringUtils.isNotBlank(query.getType()), BizItem::getType, query.getType())
                .like(StringUtils.isNotBlank(query.getLocation()), BizItem::getLocation, query.getLocation())
                .ge(query.getStartTime() != null, BizItem::getHappenTime, query.getStartTime())
                .le(query.getEndTime() != null, BizItem::getHappenTime, query.getEndTime())
                .eq(StringUtils.isNotBlank(query.getAiCategory()), BizItem::getAiCategory, query.getAiCategory())
                .eq(StringUtils.isNotBlank(query.getAiStatus()), BizItem::getAiStatus, query.getAiStatus());

        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(BizItem::getTitle, query.getKeyword())
                    .or()
                    .like(BizItem::getDescription, query.getKeyword()));
        }

        wrapper.orderByDesc(BizItem::getUpdateTime)
                .orderByDesc(BizItem::getCreateTime);

        page(page, wrapper);
        return convertToVOPage(page);
    }

    private PageResult<BizItemStatVO> convertToVOPage(Page<BizItem> page) {
        List<BizItemStatVO> voList = page.getRecords().stream()
                .map(item -> {
                    BizItemStatVO vo = new BizItemStatVO();
                    BeanUtils.copyProperties(item, vo);
                    vo.setStatusDesc(BizItemStatusEnum.getDescByCode(item.getStatus()));
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 保存物品图片
     */
    private void saveItemImages(Long itemId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        for (String url : imageUrls) {
            BizItemImage image = new BizItemImage();
            image.setItemId(itemId);
            image.setUrl(url);
            image.setCreateTime(LocalDateTime.now());
            bizItemImageDao.insert(image);
        }
    }
    /**
     * 构建分页缓存 key
     */
    private String buildPageCacheKey(ItemPageQueryDTO query) {
        return ITEM_PAGE_KEY
                + "type=" + safe(query.getType())
                + ":keyword=" + safe(query.getKeyword())
                + ":location=" + safe(query.getLocation())
                + ":start=" + query.getStartTime()
                + ":end=" + query.getEndTime()
                + ":page=" + query.getPageNum()
                + ":size=" + query.getPageSize();
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
        redisTemplate.delete(ITEM_DETAIL_KEY + itemId);

        Set<String> keys = redisTemplate.keys(ITEM_PAGE_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
