package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizPinRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物品置顶请求数据访问层
 */
@Mapper
public interface BizPinRequestDao extends BaseMapper<BizPinRequest> {
}
