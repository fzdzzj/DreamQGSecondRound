package com.qg.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SysUserStatVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private String role;
    private String roleDesc;

    private Integer status;
    private String statusDesc;

    private LocalDateTime createTime;
}
