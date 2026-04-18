package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizItemAiTag;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 物品AI标签数据访问层
 */
@Mapper
public interface BizItemAiTagDao extends BaseMapper<BizItemAiTag> {
    /**
     * 根据物品ID和AI结果版本查询物品AI标签
     *
     * @param itemId  物品ID
     * @param version AI结果版本
     * @return 物品AI标签列表
     */
    @Select("SELECT * FROM biz_item_ai_tag WHERE item_id = #{itemId} AND ai_result_version = #{version}")
    List<BizItemAiTag> selectByItemIdAndVersion(@Param("itemId") Long itemId, @Param("version") int version);
}
