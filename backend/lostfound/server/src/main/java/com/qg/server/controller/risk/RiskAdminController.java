package com.qg.server.controller.risk;

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

/**
 * 风险管理接口
 * 提供风险事件的分页查询、详情查询、处理等接口
 */
@Slf4j
@RestController
@RequestMapping("/admin/risk")
@RequiredArgsConstructor
@Tag(name = "风险监控管理接口")
public class RiskAdminController {

    private final RiskAdminService riskAdminService;

    /**
     * 分页查询风险事件
     *
     * @param pageNum      页码
     * @param pageSize     页大小
     * @param handleStatus 处理状态
     * @param riskType     风险类型
     * @return 分页结果
     */

    @GetMapping("/page")
    @Operation(summary = "分页查询风险事件")
    public Result<PageResult<BizRiskEvent>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 @RequestParam(required = false) String handleStatus,
                                                 @RequestParam(required = false) String riskType) {
        log.info("分页查询风险事件，pageNum={}, pageSize={}, handleStatus={}, riskType={}", pageNum, pageSize, handleStatus, riskType);
        PageResult<BizRiskEvent> pageResult = riskAdminService.pageRiskEvents(pageNum, pageSize, handleStatus, riskType);
        log.info("分页查询风险事件结果：{}", pageResult);
        return Result.success(pageResult);
    }

    /**
     * 查询风险事件详情
     *
     * @param id 风险事件ID
     * @return 风险事件详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询风险事件详情")
    public Result<BizRiskEvent> detail(@PathVariable Long id) {
        log.info("查询风险事件详情，id={}", id);
        BizRiskEvent riskEvent = riskAdminService.getRiskEventDetail(id);
        log.info("查询风险事件详情结果：{}", riskEvent);
        return Result.success(riskEvent);
    }

    /**
     * 处理风险事件
     *
     * @param dto 处理风险事件的DTO
     * @return 成功结果
     */
    @PostMapping("/handle")
    @Operation(summary = "处理风险事件")
    public Result<Void> handle(@RequestBody RiskHandleDTO dto) {
        log.info("处理风险事件，dto={}", dto);
        Long adminId = BaseContext.getCurrentId();
        riskAdminService.handleRiskEvent(adminId, dto);
        log.info("处理风险事件成功，adminId={}", adminId);
        return Result.success();
    }
}
