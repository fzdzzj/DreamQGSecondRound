package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.BizItemStatus;
import com.qg.common.constant.BizItemType;
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
import com.qg.pojo.entity.BizItemImage;
import com.qg.pojo.query.ItemPageQuery;
import com.qg.pojo.vo.BizItemStatVO;
import com.qg.pojo.vo.BizItemVO;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.service.ItemService;
import com.qg.server.service.NotificationService;
import com.qg.server.mapper.BizItemImageDao;
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
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<BizItemDao, BizItem> implements ItemService {

    private final BizItemImageDao bizItemImageDao;  // 物品图片数据访问层
    private final RedisTemplate<String, Object> redisTemplate; // Redis 操作

    /**
     * 详情缓存 key 前缀
     */
    private static final String ITEM_DETAIL_KEY = "item:detail:";

    /**
     * 分页缓存 key 前缀
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
        bizItem.setAiStatus("PENDING");

        // 使用 IService 提供的 save 方法
        save(bizItem);
        saveItemImages(bizItem.getId(), lostBizItemDTO.getImageUrls());

        // 新增后清理相关缓存
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
        bizItem.setAiStatus("PENDING");

        // 使用 IService 提供的 save 方法
        save(bizItem);
        saveItemImages(bizItem.getId(), lostBizItemDTO.getImageUrls());

        // 新增后清理相关缓存
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

        // 构建更新对象
        BizItem bizItem = new BizItem();
        BeanUtils.copyProperties(updateBizItemDTO, bizItem);
        bizItem.setId(id);
        bizItem.setUserId(oldItem.getUserId());
        bizItem.setType(oldItem.getType());
        bizItem.setStatus(oldItem.getStatus());

        // 修改后可重新触发 AI 分析
        bizItem.setAiStatus("PENDING");

        // 使用 IService 提供的 updateById 方法
        updateById(bizItem);

        // 删除旧图，插入新图
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
        BizItem bizItem = getById(id);  // 使用 IService 提供的 getById 方法
        if (bizItem == null) {
            log.warn("获取物品详情失败，物品不存在，itemId={}", id);
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
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

        // 4. 回写缓存
        redisTemplate.opsForValue().set(cacheKey, bizItemVO, 30, TimeUnit.MINUTES);
        log.info("写入物品详情缓存，itemId={}", id);

        return bizItemVO;
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
     * 分页查询物品列表
     */
    @Override
    public PageResult<BizItemStatVO> pageList(ItemPageQuery query) {
        Page<BizItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<BizItem> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(StringUtils.isNotBlank(query.getType()), BizItem::getType, query.getType())
                .like(StringUtils.isNotBlank(query.getLocation()), BizItem::getLocation, query.getLocation())
                .ge(query.getStartTime() != null, BizItem::getHappenTime, query.getStartTime())
                .le(query.getEndTime() != null, BizItem::getHappenTime, query.getEndTime())
                .eq(BizItem::getStatus, BizItemStatus.OPEN);

        // 关键词搜索
        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(BizItem::getTitle, query.getKeyword())
                    .or()
                    .like(BizItem::getDescription, query.getKeyword()));
        }

        wrapper.orderByDesc(BizItem::getCreateTime);

        page(page, wrapper);  // 使用 IService 提供的 page 方法
        return convertToVOPage(page);
    }

    /**
     * 物品图片保存
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
     * 清理物品缓存
     */
    private void evictItemCaches(Long itemId) {
        redisTemplate.delete(ITEM_DETAIL_KEY + itemId);
        Set<String> keys = redisTemplate.keys(ITEM_PAGE_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 转换分页结果
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
}
