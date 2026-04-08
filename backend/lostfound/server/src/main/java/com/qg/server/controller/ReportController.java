package com.qg.server.controller;

import com.qg.common.context.BaseContext;
import com.qg.common.result.PageResult;
import com.qg.common.result.Result;
import com.qg.pojo.dto.ReportAuditDTO;
import com.qg.pojo.dto.ReportDTO;
import com.qg.pojo.dto.ReportPageQueryDTO;
import com.qg.pojo.vo.ReportDetailVO;
import com.qg.pojo.vo.ReportListVO;
import com.qg.server.service.ReportService;
import com.qg.server.service.impl.ReportServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping ("/report")
@RequiredArgsConstructor
@Tag(name = "举报模块")
public class ReportController {
    private final ReportService reportService;

    /**
     * 提交举报
     */
    @PostMapping
    @Operation(summary = "提交举报")
    public Result<Void>submit(@Validated @RequestBody ReportDTO reportDTO){
        log.info("用户提交举报，itemId={}, userId={}", reportDTO.getItemId(), BaseContext.getCurrentId());
        reportService.submitReport(reportDTO);
        log.info("举报提交成功，itemId={}, userId={}", reportDTO.getItemId(), BaseContext.getCurrentId());
        return Result.success();
    }
    /**
     * 审核举报(管理员)
     */
    @PostMapping("/audit")
    @Operation(summary = "审核举报")
    public Result<Void> audit(@Validated @RequestBody ReportAuditDTO reportAuditDTO){
        log.info("管理员审核举报，reportId={}, adminId={}", reportAuditDTO.getReportId(), BaseContext.getCurrentId());
        reportService.auditReport(reportAuditDTO);
        log.info("举报审核完成，reportId={}, result={}",reportAuditDTO.getReportId(), reportAuditDTO.getStatus());
        return Result.success();
    }
    /**
     * 获取举报列表(管理员)
     */
    @PostMapping("/list")
    @Operation(summary = "获取举报列表")
    public Result<PageResult<ReportListVO>> list(@Validated @RequestBody ReportPageQueryDTO pageQueryDTO){
        log.info("管理员获取举报列表，pageNum={}, pageSize={}", pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        PageResult<ReportListVO> pageResult = reportService.list(pageQueryDTO);
        log.info("举报列表获取成功，total={}, pageNum={}, pageSize={}", pageResult.getTotal(), pageResult.getPageNum(), pageResult.getPageSize());
        return Result.success(pageResult);
    }
    /**
     * 获取举报详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取举报详情")
    public Result<ReportDetailVO> get(@PathVariable Long id){
        log.info("获取举报详情，reportId={}", id);
        ReportDetailVO report = reportService.getById(id);
        log.info("举报详情获取成功，reportId={}", id);
        return Result.success(report);
    }
}
