package com.qg.pojo.dto;

import com.qg.common.constant.RegexConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "邮箱验证码DTO")
@Data
public class EmailSendCodeDTO implements Serializable {
    @Schema(description = "邮箱")
    @Pattern(regexp = RegexConstant.EMAIL, message = "邮箱格式错误")
    @NotBlank(message = "邮箱不能为空")
    private String email;
    @Schema(description = "类型")
    @NotBlank(message = "类型不能为空,1.REGISTER / 2.LOGIN / 3.RESET_PASSWORD")
    private String type;
}
