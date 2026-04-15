package com.qg.server.service;

import com.qg.common.result.PageResult;
import com.qg.pojo.dto.LostBizItemDTO;
import com.qg.pojo.dto.UpdateBizItemDTO;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.dto.ItemPageQueryDTO;
import com.qg.pojo.vo.BizItemStatVO;
import com.qg.pojo.vo.BizItemDetailVO;
import com.baomidou.mybatisplus.extension.service.IService;  // 引入 IService

import java.util.List;

public interface ItemService extends IService<BizItem> { // 继承 IService

    /**
     * 发布丢失物品
     * @param lostBizItemDTO 丢失物品的 DTO
     */
    void publishLostItem(LostBizItemDTO lostBizItemDTO);

    /**
     * 发布拾取物品
     * @param lostBizItemDTO 拾取物品的 DTO
     */
    void publishFoundItem(LostBizItemDTO lostBizItemDTO);

    /**
     * 修改物品
     * @param id 物品ID
     * @param updateBizItemDTO 更新的物品信息
     */
    void updateItem(Long id, UpdateBizItemDTO updateBizItemDTO);

    /**
     * 获取物品详情
     * @param id 物品ID
     * @return 物品详情的 VO 对象
     */
    BizItemDetailVO getItem(Long id);

    /**
     * 删除物品
     * @param id 物品ID
     */
    void deleteItem(Long id);

    /**
     * 分页查询物品列表
     * @param query 查询条件
     * @return 物品分页列表
     */
    PageResult<BizItemStatVO> pageList(ItemPageQueryDTO query);

    /**
     * 获取当前用户发布的物品列表（分页）
     * @param query 查询条件
     * @return 物品分页列表
     */
    PageResult<BizItemStatVO> myPageList(ItemPageQueryDTO query);

    /**
     * 关闭物品（仅发布者可以关闭）
     * @param id 物品ID
     */
    void closeItem(Long id);

    void clearExpiredPinnedItems();

    void saveItemAndImages(BizItem item, List<String> imageUrls);
    void saveItemImages(Long itemId, List<String> imageUrls);
    void updateItemDb(Long id, BizItem item, List<String> imageUrls);
}
