package com.qg.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.ReportAuditDTO;
import com.qg.pojo.dto.ReportDTO;
import com.qg.pojo.dto.ReportPageQueryDTO;
import com.qg.pojo.entity.BizReport;
import com.qg.pojo.vo.ReportDetailVO;
import com.qg.pojo.vo.ReportListVO;

import java.time.LocalDateTime;

/**
 * 举报服务
 */
public interface ReportService extends IService<BizReport> {  // 继承 IService
    /**
     * 提交举报
     *
     * @param reportDTO 举报参数DTO
     */
    void submitReport(ReportDTO reportDTO);

    /**
     * 审核举报
     *
     * @param reportAuditDTO 审核参数DTO
     */
    void auditReport(Long reportId, ReportAuditDTO reportAuditDTO);

    /**
     * 分页获取举报列表
     *
     */
    PageResult<ReportListVO> page(Integer pageNum, Integer pageSize, Integer status, LocalDateTime startTime, LocalDateTime endTime, Long reporterId, Long itemId);
    /**
     * 获取举报详情
     *
     * @param id 举报ID
     * @return 举报详情
     */
    ReportDetailVO getById(Long id);
}
