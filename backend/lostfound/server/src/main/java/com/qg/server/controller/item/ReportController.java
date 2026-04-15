package com.qg.server.controller.item;

import com.qg.common.context.BaseContext;
import com.qg.common.result.PageResult;
import com.qg.common.result.Result;
import com.qg.pojo.dto.ReportAuditDTO;
import com.qg.pojo.dto.ReportDTO;
import com.qg.pojo.dto.ReportPageQueryDTO;
import com.qg.pojo.vo.ReportDetailVO;
import com.qg.pojo.vo.ReportListVO;
import com.qg.server.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 举报接口
 * 提供物品举报相关的接口，如提交举报、审核举报、获取举报列表、获取举报详情等
 */
@Slf4j
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@Tag(name = "举报模块")
public class ReportController {
    private final ReportService reportService;

    /**
     * 提交举报
     *
     * @param reportDTO 举报DTO
     * @return 提交举报结果
     */
    @PostMapping
    @Operation(summary = "提交举报")
    public Result<Void> submit(@Validated @RequestBody ReportDTO reportDTO) {
        log.info("用户提交举报，itemId={}, userId={}", reportDTO.getItemId(), BaseContext.getCurrentId());
        reportService.submitReport(reportDTO);
        log.info("举报提交成功，itemId={}, userId={}", reportDTO.getItemId(), BaseContext.getCurrentId());
        return Result.success();
    }

    /**
     * 审核举报(管理员)
     *
     * @param reportAuditDTO 举报审核DTO
     * @return 审核结果
     */
    @PutMapping("/{id}/audit")
    @Operation(summary = "审核举报")
    public Result<Void> audit(@PathVariable Long id, @Validated @RequestBody ReportAuditDTO reportAuditDTO) {
        log.info("管理员审核举报，reportId={}, adminId={}", id, BaseContext.getCurrentId());
        reportService.auditReport(id, reportAuditDTO);
        log.info("举报审核完成，reportId={}", id);
        return Result.success();
    }

    /**
     * 分页获取举报列表
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param status   举报状态
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param reporterId 举报人ID
     * @param itemId    物品ID
     * @return 举报列表分页结果
     */
    @GetMapping
    @Operation(summary = "分页获取举报列表")
    public Result<PageResult<ReportListVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                                 @RequestParam(required = false) Integer status,
                                                 @RequestParam(required = false) LocalDateTime startTime,
                                                 @RequestParam(required = false) LocalDateTime endTime,
                                                 @RequestParam(required = false) Long reporterId,
                                                 @RequestParam(required = false) Long itemId) {
        return Result.success(reportService.page(pageNum, pageSize, status, startTime, endTime, reporterId, itemId));
    }

    /**
     * 获取举报详情
     *
     * @param id 举报ID
     * @return 举报详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取举报详情")
    public Result<ReportDetailVO> get(@PathVariable Long id) {
        log.info("获取举报详情，reportId={}", id);
        ReportDetailVO report = reportService.getById(id);
        log.info("举报详情获取成功，reportId={}", id);
        return Result.success(report);
    }

}
