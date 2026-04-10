package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户详情信息")
public class SysUserDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID", example = "5")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "哈基米哈基米")
    private String username;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "")
    private String email;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "哈基米哈基米")
    private String nickname;

    /**
     * 头像
     */
    @Schema(description = "头像URL", example = "https://example.com/avatar.png")
    private String avatar;

    /**
     * 角色
     */
    @Schema(description = "角色", example = "STUDENT")
    private String role;

    /**
     * 角色描述
     */
    @Schema(description = "角色描述", example = "学生")
    private String roleDesc;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private Integer status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述", example = "正常")
    private String statusDesc;

    /**
     * 最后登录IP
     */
    @Schema(description = "最后登录IP", example = "192.168.1.1")
    private String lastLoginIp;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间", example = "2021-01-01 00:00:00")
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2021-01-01 00:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2021-01-01 00:00:00")
    private LocalDateTime updateTime;
}
