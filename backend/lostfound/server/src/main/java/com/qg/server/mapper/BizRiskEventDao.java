package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizRiskEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 风险事件数据访问层
 */
@Mapper
public interface BizRiskEventDao extends BaseMapper<BizRiskEvent> {
}
