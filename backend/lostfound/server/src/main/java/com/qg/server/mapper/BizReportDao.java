package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 举报数据访问层
 */
@Mapper
public interface BizReportDao extends BaseMapper<BizReport> {
}
