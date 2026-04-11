package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizItemAiTag;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BizItemAiTagDao extends BaseMapper<BizItemAiTag> {
    @Insert("INSERT INTO biz_item_ai_tag(item_id, ai_result_version, tag) VALUES(#{itemId}, #{version}, #{tag})")
    void insertTag(@Param("itemId") Long itemId, @Param("version") int version, @Param("tag") String tag);
    @Select("SELECT * FROM biz_item_ai_tag WHERE item_id = #{itemId} AND ai_result_version = #{version}")
    List<BizItemAiTag> selectByItemIdAndVersion(@Param("itemId") Long itemId, @Param("version") int version);
}
