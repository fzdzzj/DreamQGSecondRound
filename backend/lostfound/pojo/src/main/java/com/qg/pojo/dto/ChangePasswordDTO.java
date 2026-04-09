package com.qg.pojo.dto;

import com.qg.common.constant.RegexConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "修改密码DTO")
public class ChangePasswordDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Pattern(regexp = RegexConstant.PASSWORD, message = "密码格式错误")
    @Schema(description = "旧密码", required = true, example = "123456")
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码", required = true, example = "12345678")
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = RegexConstant.PASSWORD, message = "密码格式错误")
    private String newPassword;

    @Schema(description = "确认密码", required = true, example = "12345678")
    @NotBlank(message = "确认密码不能为空")
    @Pattern(regexp = RegexConstant.PASSWORD, message = "确认密码格式错误")
    private String confirmPassword;

    @AssertTrue(message = "密码和确认密码不相等")
    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}