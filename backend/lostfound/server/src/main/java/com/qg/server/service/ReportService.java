package com.qg.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.ReportAuditDTO;
import com.qg.pojo.dto.ReportDTO;
import com.qg.pojo.dto.ReportPageQueryDTO;
import com.qg.pojo.entity.BizReport;
import com.qg.pojo.vo.ReportDetailVO;
import com.qg.pojo.vo.ReportListVO;

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
    void auditReport(ReportAuditDTO reportAuditDTO);

    /**
     * 获取举报
     *
     * @param pageQueryDTO 分页查询参数DTO
     * @return 分页查询结果
     */
    PageResult<ReportListVO> list(ReportPageQueryDTO pageQueryDTO);

    /**
     * 获取举报详情
     *
     * @param id 举报ID
     * @return 举报详情
     */
    ReportDetailVO getById(Long id);
}
