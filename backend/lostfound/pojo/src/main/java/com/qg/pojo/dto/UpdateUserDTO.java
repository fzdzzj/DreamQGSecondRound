package com.qg.pojo.dto;

import com.qg.common.constant.RegexConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;
@Schema(description = "更新用户DTO")
@Data
public class UpdateUserDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 邮箱
     */
    @Pattern(regexp = RegexConstant.EMAIL, message = "邮箱格式错误")
    private String email;
    /**
     * 手机号
     */
    @Pattern(regexp = RegexConstant.PHONE, message = "手机号格式错误")
    private String phone;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 头像URL
     */
    private String avatar;
}
