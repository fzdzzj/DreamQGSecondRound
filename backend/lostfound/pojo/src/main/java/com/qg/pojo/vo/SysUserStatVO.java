package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户列表信息")
public class SysUserStatVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "5")
    private Long id;

    @Schema(description = "用户名/账号", example = "dseam8g")
    private String username;

    @Schema(description = "昵称", example = "小明")
    private String nickname;

    @Schema(description = "邮箱", example = "user@example.com")
    private String email;

    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    @Schema(description = "角色", example = "STUDENT")
    private String role;
    @Schema(description = "角色描述", example = "学生")
    private String roleDesc;
    @Schema(description = "状态", example = "1")
    private Integer status;
    @Schema(description = "状态描述", example = "正常")
    private String statusDesc;
    @Schema(description = "创建时间", example = "2023-01-01 12:00:00")
    private LocalDateTime createTime;
}
