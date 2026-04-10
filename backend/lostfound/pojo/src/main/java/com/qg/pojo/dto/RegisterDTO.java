package com.qg.pojo.dto;

import com.qg.common.constant.RegexConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户注册DTO")
public class RegisterDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 注册账号
     * <p>
     * 业务说明：用户注册的唯一标识，支持学生学号（3125/3225开头）或管理员ID（0025开头）<br>
     * 校验规则：
     * 1. 非空（不能为null、空字符串或全空格）；
     * 示例：3125012345（学生账号）
     * </p>
     */
    @Schema(description = "用户名", required = true, example = "dreamqg")
    @NotBlank(message = "用户名不能为空")
    private String username;



    @Schema(description = "邮箱", required = true, example = "dreamqg@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = RegexConstant.EMAIL, message = "邮箱格式错误")
    private String email;

    /**
     * 手机号
     * <p>
     * 业务说明：用户注册的手机号，用于接收短信验证码<br>
     * 校验规则：
     * 1. 非空（不能为null、空字符串或全空格）；
     * 2. 11位数字；
     * 3. 匹配正则表达式{@link RegexConstant#PHONE}
     * </p>
     */
    @Schema(description = "手机号", required = true, example = "13812345678")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = RegexConstant.PHONE, message = "手机号格式错误")
    private String phone;
    /**
     * 注册密码
     * <p>
     * 业务说明：用户注册后登录系统的密码，区分大小写<br>
     * 校验规则：
     * 1. 非空（不能为null、空字符串或全空格）；
     * 示例：Abc123、1234567890、Qwe7890
     * </p>
     */
    @Schema(description = "密码（6-10位英文数字）", required = true, example = "123456")
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = RegexConstant.PASSWORD, message = "密码格式错误")
    private String password;

    /**
     * 角色标识
     * <p>
     * 业务说明：用户注册的角色标识，1=学生，2=管理员<br>
     * 校验规则：
     * 1. 非空（不能为null、空字符串或全空格）；
     * 2. 匹配正则表达式{@link RegexConstant#ROLE}
     * </p>
     */
    @Schema(description = "角色标识", required = true, example = "1")
    @NotBlank(message = "确认密码不能为空")
    @Pattern(regexp = RegexConstant.PASSWORD, message = "确认密码格式错误")
    private String passwordConfirm;
    @Schema(description = "头像")
    @NotBlank(message = "头像不能为空")
    private String avatar;
    @Schema(description = "昵称")
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /**
     * 验证密码和确认密码是否相等
     */
    @AssertTrue(message = "密码和确认密码不相等")
    public boolean isPasswordMatch() {
        return password != null && password.equals(passwordConfirm);
    }
}