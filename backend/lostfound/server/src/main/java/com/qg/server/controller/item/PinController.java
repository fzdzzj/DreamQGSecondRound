package com.qg.server.controller.item;

import com.qg.common.result.PageResult;
import com.qg.common.result.Result;
import com.qg.pojo.dto.PinApplyDTO;
import com.qg.pojo.dto.PinAuditDTO;
import com.qg.pojo.vo.PinRequestDetailVO;
import com.qg.pojo.vo.PinRequestStatVO;
import com.qg.server.service.PinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 置顶申请接口
 * 提供物品置顶申请相关的接口，如申请置顶、审核置顶、查询置顶申请列表、取消置顶申请、查询置顶申请详情等
 */
@RestController
@RequestMapping("/pin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "置顶申请接口")
public class PinController {
    private final PinService pinService;

    /**
     * 申请置顶
     *
     * @param pinApplyDTO 置顶申请DTO
     * @return 申请置顶结果
     */
    @PostMapping("/apply")
    @Operation(summary = "申请置顶", description = "用户申请物品置顶，需要提供物品ID和申请理由。")
    public Result<Void> apply(@Validated @RequestBody PinApplyDTO pinApplyDTO) {
        log.info("申请置顶，物品ID：{}，申请理由：{}", pinApplyDTO.getItemId(), pinApplyDTO.getReason());
        pinService.apply(pinApplyDTO);
        log.info("申请置顶成功:{}", pinApplyDTO);
        return Result.success();
    }

    /**
     * 管理员审核置顶申请
     *
     * @param dto 置顶审核DTO
     * @return 审核结果
     */
    @PutMapping("/audit")
    @Operation(summary = "审核置顶")
    public Result<Void> audit(@Validated @RequestBody PinAuditDTO dto) {
        log.info("审核置顶，申请ID：{}，审核状态：{}，审核备注：{}", dto.getRequestId(), dto.getStatus(), dto.getRemark());
        pinService.audit(dto);
        log.info("审核置顶完成，申请ID：{}，审核状态：{}，审核备注：{}", dto.getRequestId(), dto.getStatus(), dto.getRemark());
        return Result.success();
    }

    /**
     * 查询置顶申请列表
     *
     * @param pageNum     页码
     * @param pageSize    每页数量
     * @param status      状态
     * @param applicantId 申请人ID
     * @param itemId      物品ID
     * @return 分页查询结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询置顶申请列表")
    public Result<PageResult<PinRequestStatVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                                     @RequestParam(required = false) Integer status,
                                                     @RequestParam(required = false) Long applicantId,
                                                     @RequestParam(required = false) Long itemId) {
        log.info("查询置顶申请列表，查询参数：pageNum={}, pageSize={}, status={}, applicantId={}, itemId={}",
                pageNum, pageSize, status, applicantId, itemId);
        PageResult<PinRequestStatVO> pageResult = pinService.queryPinRequests(pageNum, pageSize, status, applicantId, itemId);
        log.info("查询置顶申请列表，查询结果：大小={}，总记录数={}", pageResult.getList().size(), pageResult.getTotal());
        return Result.success(pageResult);
    }

    /**
     * 取消置顶申请
     *
     * @param id 置顶申请ID
     * @return 取消置顶申请结果
     */
    @PutMapping("/cancel/{id}")
    @Operation(summary = "取消置顶申请", description = "用户或管理员取消置顶申请")
    public Result<Void> cancel(@PathVariable Long id) {
        log.info("取消置顶申请，pinRequestId={}", id);
        pinService.cancelPin(id); // Service 内区分角色逻辑
        log.info("取消置顶申请成功，pinRequestId={}", id);
        return Result.success();
    }

    /**
     * 查询置顶申请详情
     *
     * @param id 置顶申请ID
     * @return 置顶申请详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询置顶申请详情")
    public Result<PinRequestDetailVO> get(@PathVariable Long id) {
        log.info("查询置顶申请详情，pinRequestId={}", id);
        PinRequestDetailVO request = pinService.getById(id);
        log.info("查询置顶申请详情成功，置顶申请详情：{}", request);
        return Result.success(request);
    }

    /**
     * 查询当前用户的置顶申请列表
     *
     * @return 当前用户的置顶申请列表
     */
    @GetMapping("/mylist")
    @Operation(summary = "查询当前用户的置顶申请列表")
    public Result<List<PinRequestStatVO>> mylist() {
        log.info("查询当前用户的置顶申请列表");
        List<PinRequestStatVO> pageResult = pinService.myList();
        log.info("查询当前用户的置顶申请列表成功，查询结果：大小={}", pageResult.size());
        return Result.success(pageResult);
    }

}
