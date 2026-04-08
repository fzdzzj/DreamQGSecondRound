package com.qg.server.controller;

import com.qg.common.result.PageResult;
import com.qg.common.result.Result;
import com.qg.pojo.dto.PinApplyDTO;
import com.qg.pojo.dto.PinAuditDTO;
import com.qg.pojo.dto.PinRequestQueryDTO;
import com.qg.pojo.entity.BizPinRequest;
import com.qg.pojo.vo.PinRequestDetailVO;
import com.qg.pojo.vo.PinRequestStatVO;
import com.qg.server.service.PinService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pin")
@RequiredArgsConstructor
@Slf4j
public class PinController {
    private final PinService pinService;

    /**
     * 申请置顶
     */
    @PostMapping("/apply")
    public Result<Void> apply(PinApplyDTO pinApplyDTO){
        log.info("申请置顶，物品ID：{}，申请理由：{}", pinApplyDTO.getItemId(), pinApplyDTO.getReason());
        pinService.apply(pinApplyDTO);
        log.info("申请置顶成功:{}", pinApplyDTO);
        return Result.success();
    }

    /**
     * 管理员审核
     */
    @PostMapping("/audit")
    @Operation(summary = "审核置顶")
    public Result<Void> audit(@Validated @RequestBody PinAuditDTO dto) {
        pinService.audit(dto);
        return Result.success();
    }
    /**
     * 查询置顶申请列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询置顶申请列表")
    public Result<PageResult<PinRequestStatVO>> page(@Validated @RequestBody PinRequestQueryDTO query) {
        PageResult<PinRequestStatVO> pageResult = pinService.queryPinRequests(query);
        return Result.success(pageResult);
    }
    @PostMapping("/cancel/{id}")
    @Operation(summary = "取消置顶申请", description = "用户或管理员取消置顶申请")
    public Result<Void> cancel(@PathVariable Long id) {
        pinService.cancelPin(id); // Service 内区分角色逻辑
        log.info("取消置顶申请，pinRequestId={}", id);
        return Result.success();
    }
    @GetMapping("/{id}")
    @Operation(summary = "查询置顶申请详情")
    public Result<PinRequestDetailVO> get(@PathVariable Long id) {
        PinRequestDetailVO request = pinService.getById(id);
        log.info("查询置顶申请详情，pinRequestId={}", id);
        log.info("置顶申请详情：{}", request);
        return Result.success(request);
    }
    @GetMapping("/mylist")
    @Operation(summary = "查询当前用户的置顶申请列表")
    public Result<List<PinRequestStatVO>> mylist() {
        List<PinRequestStatVO> pageResult = pinService.myList();
        return Result.success(pageResult);
    }

}
