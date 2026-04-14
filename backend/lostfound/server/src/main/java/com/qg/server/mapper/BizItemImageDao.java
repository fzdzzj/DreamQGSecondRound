package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizItemImage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物品图片数据访问层
 */
@Mapper
public interface BizItemImageDao extends BaseMapper<BizItemImage> {
}