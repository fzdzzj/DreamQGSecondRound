package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizItemAiResult;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BizItemAiResultDao extends BaseMapper<BizItemAiResult> {
    /**
     * 根据物品ID查询最新一次的AI结果
     * @param itemId 物品ID
     * @return 最新一次的AI结果
     */
    @Select("SELECT * FROM biz_item_ai_result WHERE item_id = #{itemId} ORDER BY result_version DESC LIMIT 1")
    BizItemAiResult selectLatestByItemId(Long itemId);
    @Select("SELECT * FROM biz_item_ai_result WHERE item_id = #{itemId}")
    List<BizItemAiResult> selectByItemId(Long itemId);

    List<BizItemAiResult> selectByItemIds(List<Long> itemIds);
}
