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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/biz/claim")
@Tag(name = "认领申请接口")
public class BizClaimRequestController {

    private final BizClaimRequestService claimRequestService;


    // 失主发起认领申请
    @PostMapping("/create")
    @Operation(summary = "失主发起认领申请")
    @AntiBot(value = LimitTypeConstant.USER_CLAIM_LIMIT)
    public Result<Void> create(@RequestBody BizClaimRequestDTO request) {
        log.info("用户 {} 创建认领申请: {}", BaseContext.getCurrentId(), request);
        claimRequestService.createClaimRequest(request);
        return Result.success();
    }

    // 查询物品的待审批申请（拾取者）
    @GetMapping("/pending")
    @Operation(summary = "查询物品的待审批申请")
    public Result<List<BizClaimRequestVO>> getPending() {
        log.info("用户 {} 查询物品的待审批申请", BaseContext.getCurrentId());
        List<BizClaimRequestVO> vos = claimRequestService.getPendingRequests();
        return Result.success(vos);
    }

    // 拾取者审批
    @PostMapping("/approve")
    @Operation(summary = "拾取者审批")
    public Result<Void> approve(@RequestBody ApproveRequestDTO approveRequestDTO) {
        log.info("用户 {} 审批认领申请: {}", BaseContext.getCurrentId(), approveRequestDTO);
        claimRequestService.approveRequest(approveRequestDTO);
        log.info("用户 {} 审批认领申请成功", BaseContext.getCurrentId());
        return Result.success();
    }
}
