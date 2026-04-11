package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.*;
import com.qg.common.context.BaseContext;
import com.qg.common.enums.BizItemStatusEnum;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.DeletionNotAllowedException;
import com.qg.common.exception.UpdateNotAllowedException;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.LostBizItemDTO;
import com.qg.pojo.dto.UpdateBizItemDTO;
import com.qg.pojo.dto.ItemPageQueryDTO;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizItemAiResult;
import com.qg.pojo.entity.BizItemImage;
import com.qg.pojo.vo.BizItemDetailVO;
import com.qg.pojo.vo.BizItemStatVO;
import com.qg.server.event.ItemAiGenerateEvent;
import com.qg.server.mapper.BizItemAiResultDao;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.mapper.BizItemImageDao;
import com.qg.server.service.ItemService;
import com.qg.server.ai.client.ImageDescriptionClient;
import com.qg.server.ai.client.ImageDescriptionClient.ImageItem;
import com.qg.server.ai.util.AiUtils;
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
    private final BizItemDao bizItemDao;
    private final ImageDescriptionClient imageDescriptionClient;
    private final AIProperties aiProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishLostItem(LostBizItemDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("发布丢失物品开始，userId={}", userId);

        BizItem item = new BizItem();
        item.setUserId(userId);
        item.setType(BizItemType.LOST);
        item.setStatus(BizItemStatus.OPEN);
        item.setIsPinned(0);
        item.setAiStatus("PENDING");
        item.setCurrentAiResultId(null);

        // 必填字段
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setLocation(dto.getLocation());
        item.setHappenTime(dto.getHappenTime());

        // ⚡ 使用 BaseMapper 插入并回写自增主键
        int rows = bizItemDao.insert(item);
        log.info("insert 返回 rows={}, itemId={}", rows, item.getId());

        if (rows <= 0 || item.getId() == null) {
            throw new IllegalStateException("插入失物记录失败，itemId 为 null");
        }

        // 保存图片
        saveItemImages(item.getId(), dto.getImageUrls());

        // 发布 AI 事件（事务提交后触发）
        applicationEventPublisher.publishEvent(new ItemAiGenerateEvent(
                this,
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getLocation(),
                userId,
                buildImageItems(dto.getImageUrls())
        ));

        // 清理缓存
        evictItemCaches(item.getId());
        log.info("发布丢失物品成功，itemId={}, userId={}", item.getId(), userId);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishFoundItem(LostBizItemDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("发布拾取物品开始，userId={}", userId);

        BizItem item = new BizItem();
        BeanUtils.copyProperties(dto, item);
        item.setUserId(userId);
        item.setType(BizItemType.FOUND);
        item.setStatus(BizItemStatus.OPEN);
        item.setIsPinned(0);
        item.setAiStatus(BizItemAiResultStatus.PENDING);
        item.setCurrentAiResultId(null);

        save(item);
        saveItemImages(item.getId(), dto.getImageUrls());

        applicationEventPublisher.publishEvent(new ItemAiGenerateEvent(this, item.getId(), dto.getTitle(), dto.getDescription(), dto.getLocation(), userId, buildImageItems(dto.getImageUrls())));

        evictItemCaches(item.getId());
        log.info("发布拾取物品成功，itemId={}, userId={}", item.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(Long id, UpdateBizItemDTO dto) {
        Long userId = BaseContext.getCurrentId();
        log.info("修改物品开始，itemId={}, userId={}", id, userId);

        BizItem oldItem = getById(id);
        if (oldItem == null) throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        if (!oldItem.getUserId().equals(userId)) throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);
        if (BizItemStatus.REPORTED.equals(oldItem.getStatus()) || BizItemStatus.CLOSED.equals(oldItem.getStatus()))
            throw new UpdateNotAllowedException(MessageConstant.UPDATE_NOT_ALLOWED);

        BizItem item = new BizItem();
        BeanUtils.copyProperties(dto, item);
        item.setId(id);
        item.setUserId(oldItem.getUserId());
        item.setType(oldItem.getType());
        item.setStatus(oldItem.getStatus());
        item.setIsPinned(oldItem.getIsPinned());
        item.setPinExpireTime(oldItem.getPinExpireTime());

        // 清空旧 AI 结果
        item.setAiStatus(BizItemAiResultStatus.PENDING);
        item.setCurrentAiResultId(null);

        updateById(item);

        bizItemImageDao.delete(new LambdaQueryWrapper<BizItemImage>().eq(BizItemImage::getItemId, id));
        saveItemImages(id, dto.getImageUrls());

        applicationEventPublisher.publishEvent(new ItemAiGenerateEvent(this, id, dto.getTitle(), dto.getDescription(), dto.getLocation(), userId, buildImageItems(dto.getImageUrls())));
        evictItemCaches(id);

        log.info("修改物品成功，itemId={}, userId={}", id, userId);
    }

    @Override
    public BizItemDetailVO getItem(Long id) {
        String cacheKey = RedisConstant.ITEM_DETAIL_KEY + id;
        Object cache = redisTemplate.opsForValue().get(cacheKey);
        if (cache instanceof BizItemDetailVO vo) return vo;

        BizItem item = getById(id);
        if (item == null || BizItemStatus.REPORTED.equals(item.getStatus()))
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);
        if (BizItemStatus.CLOSED.equals(item.getStatus()) && !item.getUserId().equals(BaseContext.getCurrentId()))
            throw new AbsentException(MessageConstant.ITEM_NOT_FOUND);

        BizItemDetailVO detail = new BizItemDetailVO();
        BeanUtils.copyProperties(item, detail);

        List<String> images = bizItemImageDao.selectList(new LambdaQueryWrapper<BizItemImage>().eq(BizItemImage::getItemId, id).orderByAsc(BizItemImage::getId))
                .stream().map(BizItemImage::getUrl).toList();
        detail.setImageUrls(images);

        if (item.getCurrentAiResultId() != null) {
            BizItemAiResult aiResult = bizItemAiResultDao.selectById(item.getCurrentAiResultId());
            if (aiResult != null) {
                detail.setAiCategory(aiResult.getAiCategory());
                detail.setAiTags(aiResult.getAiTags());
                detail.setAiDescription(aiResult.getAiDescription());
                detail.setAiStatus(aiResult.getStatus());
            }
        }

        detail.setStatusDesc(BizItemStatusEnum.getDescByCode(item.getStatus()));
        redisTemplate.opsForValue().set(cacheKey, detail, 30, TimeUnit.MINUTES);
        return detail;
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

        if (!BizItemStatus.CLOSED.equals(item.getStatus())) {
            item.setStatus(BizItemStatus.CLOSED);
            updateById(item);
            evictItemCaches(id);
        }
    }

    @Override
    public PageResult<BizItemStatVO> pageList(ItemPageQueryDTO query) {
        boolean cacheable = query.getPageNum() != null && query.getPageNum() == 1
                && query.getPageSize() != null && query.getPageSize() <= 10;

        String cacheKey = cacheable ? buildPageCacheKey(query) : null;
        Object cache = cacheKey != null ? redisTemplate.opsForValue().get(cacheKey) : null;
        if (cache instanceof PageResult<?> pageResult) {
            @SuppressWarnings("unchecked")
            PageResult<BizItemStatVO> cached = (PageResult<BizItemStatVO>) pageResult;
            log.info("命中物品分页缓存，key={}", cacheKey);
            return cached;
        }

        Page<BizItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<BizItem> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(query.getType() != null, BizItem::getType, query.getType())
                .like(query.getLocation() != null, BizItem::getLocation, query.getLocation())
                .ge(query.getStartTime() != null, BizItem::getHappenTime, query.getStartTime())
                .le(query.getEndTime() != null, BizItem::getHappenTime, query.getEndTime())
                .eq(query.getAiStatus() != null, BizItem::getAiStatus, query.getAiStatus())
                .eq(BizItem::getStatus, BizItemStatus.OPEN)
                .orderByDesc(BizItem::getIsPinned)
                .orderByDesc(BizItem::getPinExpireTime)
                .orderByDesc(BizItem::getCreateTime);

        if (query.getKeyword() != null) {
            wrapper.and(w -> w.like(BizItem::getTitle, query.getKeyword())
                    .or()
                    .like(BizItem::getDescription, query.getKeyword()));
        }

        page(page, wrapper);

        // VO 转换并回填最新 AI 结果
        List<BizItemStatVO> voList = page.getRecords().stream().map(item -> {
            BizItemStatVO vo = new BizItemStatVO();
            BeanUtils.copyProperties(item, vo);
            vo.setStatusDesc(BizItemStatusEnum.getDescByCode(item.getStatus()));

            if (item.getCurrentAiResultId() != null) {
                BizItemAiResult aiResult = bizItemAiResultDao.selectById(item.getCurrentAiResultId());
                if (aiResult != null) {
                    vo.setAiCategory(aiResult.getAiCategory());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        PageResult<BizItemStatVO> result = new PageResult<>(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());

        if (cacheable && cacheKey != null) {
            redisTemplate.opsForValue().set(cacheKey, result, 5, TimeUnit.MINUTES);
            log.info("写入物品分页缓存，key={}", cacheKey);
        }

        return result;
    }


    @Override
    public PageResult<BizItemStatVO> myPageList(ItemPageQueryDTO query) {
        Long userId = BaseContext.getCurrentId();
        Page<BizItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<BizItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizItem::getUserId, userId)
                .eq(query.getType() != null, BizItem::getType, query.getType())
                .like(query.getLocation() != null, BizItem::getLocation, query.getLocation())
                .ge(query.getStartTime() != null, BizItem::getHappenTime, query.getStartTime())
                .le(query.getEndTime() != null, BizItem::getHappenTime, query.getEndTime())
                .eq(query.getAiStatus() != null, BizItem::getAiStatus, query.getAiStatus());

        if (query.getKeyword() != null) {
            wrapper.and(w -> w.like(BizItem::getTitle, query.getKeyword())
                    .or()
                    .like(BizItem::getDescription, query.getKeyword()));
        }

        page(page, wrapper);
        return convertToVOPage(page);
    }

    private PageResult<BizItemStatVO> convertToVOPage(Page<BizItem> page) {
        List<BizItemStatVO> list = page.getRecords().stream().map(item -> {
            BizItemStatVO vo = new BizItemStatVO();
            BeanUtils.copyProperties(item, vo);
            vo.setStatusDesc(BizItemStatusEnum.getDescByCode(item.getStatus()));
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

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
        List<ImageItem> items = new ArrayList<>();
        for (String u : urls) items.add(new ImageItem(u, "主图"));
        return items;
    }

    private String buildPageCacheKey(ItemPageQueryDTO query) {
        return RedisConstant.ITEM_PAGE_KEY
                + ":type=" + safe(query.getType())
                + ":location=" + safe(query.getLocation())
                + ":keyword=" + safe(query.getKeyword())
                + ":aiStatus=" + safe(query.getAiStatus())
                + ":aiCategory=" + safe(query.getAiCategory())
                + ":start=" + (query.getStartTime() != null ? query.getStartTime().toString() : "")
                + ":end=" + (query.getEndTime() != null ? query.getEndTime().toString() : "")
                + ":page=" + query.getPageNum()
                + ":size=" + query.getPageSize();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
    private void evictItemCaches(Long itemId) {
        redisTemplate.delete("ITEM_DETAIL_KEY:" + itemId);
        Set<String> keys = redisTemplate.keys("ITEM_PAGE_KEY:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

}
