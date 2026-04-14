package com.qg.server.controller.risk;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qg.common.context.BaseContext;
import com.qg.common.result.PageResult;
import com.qg.common.result.Result;
import com.qg.pojo.dto.RiskHandleDTO;
import com.qg.pojo.entity.BizRiskEvent;
import com.qg.server.service.RiskAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/risk")
@RequiredArgsConstructor
@Tag(name = "风险监控管理接口")
public class RiskAdminController {

    private final RiskAdminService riskAdminService;

    @GetMapping("/page")
    @Operation(summary = "分页查询风险事件")
    public Result<PageResult<BizRiskEvent>> page(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 @RequestParam(required = false) String handleStatus,
                                                 @RequestParam(required = false) String riskType) {
        log.info("分页查询风险事件，pageNum={}, pageSize={}, handleStatus={}, riskType={}", page, pageSize, handleStatus, riskType);
        PageResult<BizRiskEvent> pageResult = riskAdminService.pageRiskEvents(page, pageSize, handleStatus, riskType);
        log.info("分页查询风险事件结果：{}", pageResult);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询风险事件详情")
    public Result<BizRiskEvent> detail(@PathVariable Long id) {
        log.info("查询风险事件详情，id={}", id);
        BizRiskEvent riskEvent = riskAdminService.getRiskEventDetail(id);
        log.info("查询风险事件详情结果：{}", riskEvent);
        return Result.success(riskEvent);
    }

    @PostMapping("/handle")
    @Operation(summary = "处理风险事件")
    public Result<Void> handle(@RequestBody RiskHandleDTO dto) {
        log.info("处理风险事件，dto={}", dto);
        Long adminId = BaseContext.getCurrentId();
        riskAdminService.handleRiskEvent(adminId, dto);
        log.info("处理风险事件成功");
        return Result.success();
    }
}
