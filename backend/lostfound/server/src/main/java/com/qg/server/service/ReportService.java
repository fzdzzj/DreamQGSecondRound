package com.qg.server.service;

import com.qg.common.result.PageResult;
import com.qg.pojo.dto.ReportAuditDTO;
import com.qg.pojo.dto.ReportDTO;
import com.qg.pojo.dto.ReportPageQueryDTO;
import com.qg.pojo.entity.BizReport;
import com.qg.pojo.vo.ReportDetailVO;
import com.qg.pojo.vo.ReportListVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ReportService extends IService<BizReport> {  // 继承 IService

    void submitReport(ReportDTO reportDTO);

    void auditReport(ReportAuditDTO reportAuditDTO);

    PageResult<ReportListVO> list(ReportPageQueryDTO pageQueryDTO);

    ReportDetailVO getById(Long id);
}
