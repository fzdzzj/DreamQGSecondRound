package com.qg.pojo.dto;

import com.qg.common.constant.RegexConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank(message = "登录标识不能为空")
    @Pattern(regexp = RegexConstant.IDENTIFIER,
            message = "请输入有效的邮箱或用户名（4-20位字母数字）")
    private String identifier;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20位")
    private String password;

    /**
     * 判断是否为邮箱
     */
    public boolean isEmail() {
        return identifier != null && identifier.contains("@");
    }
}
