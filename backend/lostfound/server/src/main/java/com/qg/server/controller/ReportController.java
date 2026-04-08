package com.qg.server.controller;

import com.qg.common.result.Result;
import com.qg.pojo.dto.ReportAuditDTO;
import com.qg.pojo.dto.ReportDTO;
import com.qg.pojo.vo.ReportListVO;
import com.qg.server.service.ReportService;
import com.qg.server.service.impl.ReportServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        reportService.submitReport(reportDTO);
        return Result.success();
    }
    /**
     * 审核举报(管理员)
     */
    @PostMapping("/audit")
    @Operation(summary = "审核举报")
    public Result<Void> audit(@Validated @RequestBody ReportAuditDTO reportAuditDTO){
        reportService.auditReport(reportAuditDTO);
        return Result.success();
    }
    /**
     * 获取举报列表(管理员)
     */
    @PostMapping("/list")
    @Operation(summary = "获取举报列表")
    public Result<List<ReportListVO>> list(){
        return Result.success(reportService.list());
    }
}
