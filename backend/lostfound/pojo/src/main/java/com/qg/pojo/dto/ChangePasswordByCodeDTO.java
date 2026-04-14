package com.qg.pojo.dto;

import com.qg.common.constant.RegexConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "验证码修改密码")
public class ChangePasswordByCodeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "邮箱不能为空")
    @Schema(description = "邮箱")
    @Pattern(regexp = RegexConstant.EMAIL, message = "邮箱格式错误")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码")
    @Pattern(regexp = RegexConstant.CODE, message = "验证码格式错误")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = RegexConstant.PASSWORD, message = "密码格式错误")
    @Schema(description = "新密码")
    private String newPassword;
}