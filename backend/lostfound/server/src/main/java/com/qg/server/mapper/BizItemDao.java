package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BizItemDao extends BaseMapper<BizItem> {
    // 不需要写 insert()，BaseMapper 自带
}
