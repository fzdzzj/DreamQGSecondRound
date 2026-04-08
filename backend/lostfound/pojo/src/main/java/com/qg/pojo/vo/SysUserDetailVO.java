package com.qg.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SysUserDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色
     */
    private String role;

    /**
     * 角色描述
     */
    private String roleDesc;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
