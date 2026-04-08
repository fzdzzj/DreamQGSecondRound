package com.qg.server.service;

import com.qg.common.result.PageResult;
import com.qg.pojo.dto.ReportAuditDTO;
import com.qg.pojo.dto.ReportDTO;
import com.qg.pojo.dto.ReportPageQueryDTO;
import com.qg.pojo.vo.ReportDetailVO;
import com.qg.pojo.vo.ReportListVO;

import java.util.List;

public interface ReportService {
    void submitReport(ReportDTO reportDTO);

    void auditReport(ReportAuditDTO reportAuditDTO);

    PageResult<ReportListVO> list(ReportPageQueryDTO pageQueryDTO);

    ReportDetailVO getById(Long id);
}
