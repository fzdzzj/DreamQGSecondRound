package com.qg.server.controller.message;

import com.qg.common.constant.LimitTypeConstant;
import com.qg.common.context.BaseContext;
import com.qg.common.result.Result;
import com.qg.pojo.dto.ApproveRequestDTO;
import com.qg.pojo.dto.BizClaimRequestDTO;
import com.qg.pojo.vo.BizClaimRequestVO;
import com.qg.server.anno.AntiBot;
import com.qg.server.service.BizClaimRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认领申请接口
 * 用于失主发起认领申请、拾取者审批认领申请、查询物品的待审批申请等操作。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/biz/claim")
@Tag(name = "认领申请接口")
public class BizClaimRequestController {

    private final BizClaimRequestService claimRequestService;


    /**
     * 失主发起认领申请
     *
     * @param request 认领申请请求参数
     */
    @PostMapping
    @Operation(summary = "失主发起认领申请")
    @AntiBot(value = LimitTypeConstant.USER_CLAIM_LIMIT)
    public Result<Void> create(@RequestBody BizClaimRequestDTO request) {
        log.info("用户 {} 创建认领申请", BaseContext.getCurrentId());
        claimRequestService.createClaimRequest(request);
        log.info("用户 {} 创建认领申请成功", BaseContext.getCurrentId());
        return Result.success();
    }

    /**
     * 查询物品的待审批申请（拾取者）
     */
    @GetMapping("/pending")
    @Operation(summary = "查询物品的待审批申请")
    public Result<List<BizClaimRequestVO>> getPending() {
        log.info("用户 {} 查询物品的待审批申请", BaseContext.getCurrentId());
        List<BizClaimRequestVO> vos = claimRequestService.getPendingRequests();
        log.info("用户 {} 获取物品的待审批申请成功，数量为 {}", BaseContext.getCurrentId(), vos.size());
        return Result.success(vos);
    }

    /**
     * 拾取者审批认领申请
     *
     * @param approveRequestDTO 审批认领申请请求参数
     */
    @PostMapping("/{id}/audit")
    @Operation(summary = "拾取者审批")
    public Result<Void> approve(@PathVariable Long id, @RequestBody ApproveRequestDTO approveRequestDTO) {
        log.info("用户 {} 审批认领申请: {}", BaseContext.getCurrentId(), approveRequestDTO);
        claimRequestService.approveRequest(id, approveRequestDTO);
        log.info("用户 {} 审批认领申请成功", BaseContext.getCurrentId());
        return Result.success();
    }
}
