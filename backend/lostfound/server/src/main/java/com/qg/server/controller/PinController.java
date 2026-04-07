package com.qg.server.controller;

import com.qg.common.result.Result;
import com.qg.pojo.dto.PinApplyDTO;
import com.qg.server.service.PinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
