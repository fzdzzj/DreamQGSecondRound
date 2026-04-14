package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizItemAiResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 物品AI结果数据访问层
 */
@Mapper
public interface BizItemAiResultDao extends BaseMapper<BizItemAiResult> {
    /**
     * 根据物品ID查询最新一次的AI结果
     *
     * @param itemId 物品ID
     * @return 最新一次的AI结果
     */
    @Select("SELECT * FROM biz_item_ai_result WHERE item_id = #{itemId} ORDER BY result_version DESC LIMIT 1")
    BizItemAiResult selectLatestByItemId(Long itemId);

    /**
     * 根据物品ID查询所有的AI结果
     *
     * @param itemId 物品ID
     * @return 所有的AI结果
     */
    @Select("SELECT * FROM biz_item_ai_result WHERE item_id = #{itemId}")
    List<BizItemAiResult> selectByItemId(Long itemId);

    /**
     * 根据物品ID列表查询所有的AI结果
     *
     * @param itemIds 物品ID列表
     * @return 所有的AI结果
     */
    List<BizItemAiResult> selectByItemIds(List<Long> itemIds);
}
