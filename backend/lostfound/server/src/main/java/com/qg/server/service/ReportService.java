package com.qg.server.service;

import com.qg.pojo.dto.ReportAuditDTO;
import com.qg.pojo.dto.ReportDTO;

public interface ReportService {
    void submitReport(ReportDTO reportDTO);

    void auditReport(ReportAuditDTO reportAuditDTO);
}
