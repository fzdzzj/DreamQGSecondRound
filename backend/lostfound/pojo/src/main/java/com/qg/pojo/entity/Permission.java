package com.qg.pojo.entity;

import lombok.Data;
/**
 * 权限实体类
 */
@Data
public class Permission {
    private Integer id;
    private String permName;
    private String method;
    private String url;
}
