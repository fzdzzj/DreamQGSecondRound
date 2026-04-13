package com.qg.server.controller.message;

import com.qg.common.context.BaseContext;
import com.qg.common.result.Result;
import com.qg.pojo.dto.ApproveRequestDTO;
import com.qg.pojo.dto.BizClaimRequestDTO;
import com.qg.pojo.vo.BizClaimRequestVO;
import com.qg.server.service.BizClaimRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/biz/claim")
public class BizClaimRequestController {

    private final BizClaimRequestService claimRequestService;


    // 失主发起认领申请
    @PostMapping("/create")
    public BizClaimRequestVO create(@RequestBody BizClaimRequestDTO request) {
        request.setApplicantId(BaseContext.getCurrentId()); // 自动设置申请人
        return claimRequestService.createClaimRequest(request);
    }

    // 查询物品的待审批申请（拾取者）
    @GetMapping("/pending/{itemId}")
    public List<BizClaimRequestVO> getPending(@PathVariable Long itemId) {
        return claimRequestService.getPendingRequestsByItem(itemId);
    }

    // 拾取者审批
    @PostMapping("/approve/{requestId}")
    public Result<Void> approve(@RequestBody ApproveRequestDTO approveRequestDTO) {
        claimRequestService.approveRequest(approveRequestDTO);
        return Result.success();
    }
}
