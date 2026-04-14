package com.qg.server.controller.message;

import com.qg.common.context.BaseContext;
import com.qg.common.result.Result;
import com.qg.pojo.dto.EmailSendCodeDTO;
import com.qg.pojo.dto.EmailVerifyCodeDTO;
import com.qg.server.service.EmailVerificationCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name ="邮箱验证码接口", description = "邮箱验证码接口")
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

    @PostMapping("/verifyCode")
    @Operation(summary = "验证邮箱验证码")
    public Result<Void> verifyCode(@RequestBody @Validated EmailVerifyCodeDTO dto) {
        log.info("验证邮箱验证码，邮箱：{}，类型：{}，验证码：{}", dto.getEmail(), dto.getType(), dto.getCode());
        boolean ok = codeService.verifyCode(dto.getEmail(), dto.getType(), dto.getCode());
        log.info("验证邮箱验证码成功，结果：{}", ok);
        return ok ? Result.success() : Result.error(401,"验证码错误或已过期");
    }
}
