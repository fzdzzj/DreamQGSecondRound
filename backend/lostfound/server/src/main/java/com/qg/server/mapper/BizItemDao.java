package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物品数据访问层
 */
@Mapper
public interface BizItemDao extends BaseMapper<BizItem> {
}
