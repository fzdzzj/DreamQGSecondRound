package com.qg.pojo.vo;


import lombok.Data;

import java.io.Serializable;

@Data
public class SysUserVO implements Serializable {
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
     * 头像URL
     */
    private String avatar;

    /**
     * 角色: STUDENT, ADMIN, SYSTEM
     */
    private String role;

}
