package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户详情信息")
public class SysUserDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID", example = "5")
    private Long id;

    @Schema(description = "用户名", example = "哈基米哈基米")
    private String username;

    @Schema(description = "邮箱", example = "")
    private String email;

    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    @Schema(description = "昵称", example = "哈基米哈基米", nullable = true)
    private String nickname;

    @Schema(description = "头像URL", example = "https://example.com/avatar.png", nullable = true)
    private String avatar;

    @Schema(description = "角色", example = "STUDENT")
    private String role;

    @Schema(description = "角色描述", example = "学生")
    private String roleDesc;

    @Schema(description = "状态,1:正常,0:禁用", example = "1")
    private Integer status;

    @Schema(description = "状态描述", example = "正常")
    private String statusDesc;

    @Schema(description = "最后登录IP", example = "192.168.1.1", nullable = true)
    private String lastLoginIp;

    @Schema(description = "最后登录时间", example = "2021-01-01 00:00:00", nullable = true)
    private LocalDateTime lastLoginTime;

    @Schema(description = "创建时间", example = "2021-01-01 00:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2021-01-01 00:00:00")
    private LocalDateTime updateTime;
}
