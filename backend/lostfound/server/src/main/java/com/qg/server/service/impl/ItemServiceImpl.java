package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qg.common.constant.BizItemStatus;
import com.qg.common.constant.BizItemType;
import com.qg.common.constant.MessageConstant;
import com.qg.common.constant.PinConstant;
import com.qg.common.context.BaseContext;
import com.qg.common.enums.BizItemStatusEnum;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.DeletionNotAllowedException;
import com.qg.common.exception.UpdateNotAllowedException;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.LostBizItemDTO;
import com.qg.pojo.dto.UpdateBizItemDTO;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizItemImage;
import com.qg.pojo.query.ItemPageQuery;
import com.qg.pojo.vo.BizItemStatVO;
import com.qg.pojo.vo.BizItemVO;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.mapper.BizItemImageDao;
import com.qg.server.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 物品业务实现类
 *
 * 说明：
 * 1. 负责失物/拾物的发布、修改、删除、详情、分页查询
 * 2. 主表 biz_item 与图片表 biz_item_image 的写操作统一放在事务中
 * 3. 对热点数据使用 Redis 做缓存，减少首页列表和详情页的重复查库
 * 4. RBAC 只解决“接口能不能进来”，这里仍然保留“数据归属校验”
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final BizItemDao bizItemDao;
    private final BizItemImageDao bizItemImageDao;
    private final RedisTemplate<String, Object> redisTemplate;

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
     *
     * 设计说明：
     * 1. 新发布的物品默认状态为 OPEN，表示可公开检索
     * 2. AI 状态默认置为 PENDING，后续接 Spring AI 异步分析时可直接扩展
     * 3. 主表和图片表放在同一事务中，避免主表成功、图片失败导致脏数据
     * 4. 发布后主动清理相关缓存，保证首页能尽快看到新数据
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
        bizItem.setAiStatus("PENDING");

        bizItemDao.insert(bizItem);
        saveItemImages(bizItem.getId(), lostBizItemDTO.getImageUrls());

        // 新增后清理首页热点缓存
        evictItemCaches(bizItem.getId());

        log.info("发布丢失物品成功，itemId={}, userId={}", bizItem.getId(), userId);
    }

    /**
     * 发布拾取物品
     *
     * 与发布丢失物品逻辑一致，只是 type 不同。
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
        bizItem.setAiStatus("PENDING");

        bizItemDao.insert(bizItem);
        saveItemImages(bizItem.getId(), lostBizItemDTO.getImageUrls());

        // 新增后清理首页热点缓存
        evictItemCaches(bizItem.getId());

        log.info("发布拾取物品成功，itemId={}, userId={}", bizItem.getId(), userId);
    }

    /**
     * 修改物品
     *
     * 设计说明：
     * 1. 只有发布者本人才能修改自己的物品
     * 2. 已举报、已关闭的数据不允许再修改
     * 3. 图片采用“先删后插”的简化策略，适合当前项目周期
     * 4. 修改后清理详情缓存与分页缓存，避免读取旧数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(Long id, UpdateBizItemDTO updateBizItemDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("修改物品开始，itemId={}, userId={}", id, userId);

        BizItem oldItem = bizItemDao.selectById(id);
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

        // 修改后可重新触发 AI 分析
        bizItem.setAiStatus("PENDING");

        bizItemDao.updateById(bizItem);

        // 图片采用全量替换策略：先删旧图，再插新图
        bizItemImageDao.delete(
                new LambdaQueryWrapper<BizItemImage>()
                        .eq(BizItemImage::getItemId, id)
        );
        saveItemImages(id, updateBizItemDTO.getImageUrls());

        // 修改后清理缓存
        evictItemCaches(id);

        log.info("修改物品成功，itemId={}, userId={}", id, userId);
    }

    /**
     * 获取物品详情
     *
     * 设计说明：
     * 1. 优先查 Redis，减少热点详情重复查库
     * 2. 已举报、已删除的数据不对外展示
     * 3. 关闭状态下，仅发布者本人可见，避免对外展示已结束数据
     * 4. 详情缓存 30 分钟，兼顾性能与数据新鲜度
     */
    @Override
    public BizItemVO getItem(Long id) {
        String cacheKey = ITEM_DETAIL_KEY + id;

        // 1. 先查缓存
        Object cache = redisTemplate.opsForValue().get(cacheKey);
        if (cache instanceof BizItemVO bizItemVO) {
            log.info("命中物品详情缓存，itemId={}", id);
            return bizItemVO;
        }

        // 2. 查数据库
        BizItem bizItem = bizItemDao.selectById(id);
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
        BizItemVO bizItemVO = new BizItemVO();
        BeanUtils.copyProperties(bizItem, bizItemVO);

        List<String> imageUrls = bizItemImageDao.selectList(
                new LambdaQueryWrapper<BizItemImage>()
                        .eq(BizItemImage::getItemId, id)
                        .orderByAsc(BizItemImage::getId)
        ).stream().map(BizItemImage::getUrl).toList();

        bizItemVO.setImageUrls(imageUrls);
        bizItemVO.setStatusDesc(BizItemStatusEnum.getDescByCode(bizItem.getStatus()));

        // 4. 回写缓存
        redisTemplate.opsForValue().set(cacheKey, bizItemVO, 30, TimeUnit.MINUTES);
        log.info("写入物品详情缓存，itemId={}", id);

        return bizItemVO;
    }

    /**
     * 删除物品
     *
     * 设计说明：
     * 1. 只允许删除本人发布的物品
     * 2. 当前使用逻辑删除，保留审计痕迹
     * 3. 删除后同步清理详情缓存与分页缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("删除物品开始，itemId={}, userId={}", id, userId);

        BizItem bizItem = bizItemDao.selectById(id);
        if (bizItem == null) {
            log.warn("删除物品失败，物品不存在，itemId={}", id);
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        if (!bizItem.getUserId().equals(userId)) {
            log.warn("删除物品失败，无权删除他人物品，itemId={}, userId={}", id, userId);
            throw new DeletionNotAllowedException(MessageConstant.DELETE_NOT_ALLOWED);
        }

        bizItemDao.deleteById(id);
        // 删除后清理缓存
        evictItemCaches(id);

        log.info("删除物品成功，itemId={}, userId={}", id, userId);
    }

    /**
     * 分页查询物品列表
     *
     * 设计说明：
     * 1. 支持类型、地点、时间范围、关键词联合筛选
     * 2. 关键词匹配标题和描述，满足题目“按物品名称搜索”的基本诉求
     * 3. 排序规则：置顶优先，其次按发布时间倒序
     * 4. 只缓存首页第一页、小页数结果，因为它们最容易成为热点数据
     * 5. 分页缓存 5 分钟，发布/修改/删除后统一清理
     */
    @Override
    public PageResult<BizItemStatVO> pageList(ItemPageQuery query) {

        // 只缓存首页第一页、小页数结果，避免缓存太多分页数据
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

        // 基础筛选条件
        wrapper.eq(StringUtils.isNotBlank(query.getType()), BizItem::getType, query.getType())
                .like(StringUtils.isNotBlank(query.getLocation()), BizItem::getLocation, query.getLocation())
                .ge(query.getStartTime() != null, BizItem::getHappenTime, query.getStartTime())
                .le(query.getEndTime() != null, BizItem::getHappenTime, query.getEndTime())
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

        bizItemDao.selectPage(page, wrapper);

        PageResult<BizItemStatVO> result = convertToVOPage(page);

        // 首页热点缓存写回 Redis
        if (cacheable && cacheKey != null) {
            redisTemplate.opsForValue().set(cacheKey, result, 5, TimeUnit.MINUTES);
            log.info("写入物品分页缓存，key={}", cacheKey);
        }

        return result;
    }

    @Override
    public PageResult<BizItemStatVO> myPageList(ItemPageQuery query) {
        Long userId = BaseContext.getCurrentId();

        Page<BizItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<BizItem> wrapper = new LambdaQueryWrapper<>();

        // 只查当前登录用户自己的数据
        wrapper.eq(BizItem::getUserId, userId);

        // 个人列表通常允许看自己的各种状态数据，不只 OPEN
        wrapper.eq(StringUtils.isNotBlank(query.getType()), BizItem::getType, query.getType())
                .like(StringUtils.isNotBlank(query.getLocation()), BizItem::getLocation, query.getLocation())
                .ge(query.getStartTime() != null, BizItem::getHappenTime, query.getStartTime())
                .le(query.getEndTime() != null, BizItem::getHappenTime, query.getEndTime());

        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(BizItem::getTitle, query.getKeyword())
                    .or()
                    .like(BizItem::getDescription, query.getKeyword()));
        }

        // 我的物品按最新更新时间/创建时间看更合理
        wrapper.orderByDesc(BizItem::getUpdateTime)
                .orderByDesc(BizItem::getCreateTime);

        bizItemDao.selectPage(page, wrapper);
        return convertToVOPage(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeItem(Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("关闭物品开始，itemId={}, userId={}", id, userId);

        BizItem bizItem = bizItemDao.selectById(id);
        if (bizItem == null) {
            log.warn("关闭物品失败，物品不存在，itemId={}", id);
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        }

        // 只能关闭自己发布的物品
        if (!bizItem.getUserId().equals(userId)) {
            log.warn("关闭物品失败，无权操作他人物品，itemId={}, userId={}", id, userId);
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        }

        // 已关闭就不重复关闭
        if (BizItemStatus.CLOSED.equals(bizItem.getStatus())) {
            log.info("物品已是关闭状态，无需重复关闭，itemId={}", id);
            return;
        }

        // 已删除 / 已举报 状态不允许关闭
        if (BizItemStatus.REPORTED.equals(bizItem.getStatus())) {

            log.warn("关闭物品失败，当前状态不允许关闭，itemId={}, status={}", id, bizItem.getStatus());
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        }

        BizItem updateItem = new BizItem();
        updateItem.setId(id);
        updateItem.setStatus(BizItemStatus.CLOSED);

        bizItemDao.updateById(updateItem);

        // 关闭后清理缓存
        evictItemCaches(id);

        log.info("关闭物品成功，itemId={}, userId={}", id, userId);
    }


    /**
     * 把 MP 的分页结果转成统一分页返回对象
     */
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
     * 保存图片关联关系
     *
     * 当前实现采用逐条插入
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
     *
     * 说明：
     * - 把查询条件拼进 key，避免不同筛选条件相互污染
     * - 当前主要服务于首页热点分页缓存
     */
    private String buildPageCacheKey(ItemPageQuery query) {
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
     *
     * 设计说明：
     * 1. 删除当前物品详情缓存
     * 2. 删除首页分页相关缓存
     *
     * 备注：
     * - 当前为了开发效率，采用逐条删除，简单直接
     */
    private void evictItemCaches(Long itemId) {
        redisTemplate.delete(ITEM_DETAIL_KEY + itemId);

        Set<String> keys = redisTemplate.keys(ITEM_PAGE_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
