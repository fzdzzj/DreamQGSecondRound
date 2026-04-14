package com.qg.server.controller.message;

import com.qg.common.context.BaseContext;
import com.qg.common.result.Result;
import com.qg.pojo.dto.EmailSendCodeDTO;
import com.qg.server.service.EmailVerificationCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Schema(description = "邮箱验证码")
@RequestMapping("/email")
@RestController
@RequiredArgsConstructor
public class EmailController {
    private final EmailVerificationCodeService codeService;
    @PostMapping("/sendCode")
    @Operation(summary = "发送邮箱验证码")
    public Result<Void> sendCode(@RequestBody @Validated EmailSendCodeDTO dto) {
        log.info("发送邮箱验证码，邮箱：{}，类型：{}", dto.getEmail(), dto.getType());
        codeService.sendCode(dto.getEmail(), dto.getType());
        log.info("发送邮箱验证码成功");
        return Result.success();
    }
}
