package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizClaimRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 认领申请数据访问层
 */
@Mapper
public interface BizClaimRequestDao extends BaseMapper<BizClaimRequest> {
}
