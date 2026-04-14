package com.qg.pojo.dto;

import com.qg.common.constant.RegexConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "邮箱验证码验证请求参数")
@Data
public class EmailVerifyCodeDTO {
    @Schema(description = "邮箱")
    @Pattern(regexp= RegexConstant.EMAIL, message = "邮箱格式错误")
    private String email;
    @Schema(description = "验证码")
    @Pattern(regexp= RegexConstant.CODE, message = "验证码格式错误")
    private String code;
    @Schema(description = "验证码类型,1:注册,2:登录,3:修改密码")
    private String type;
}
