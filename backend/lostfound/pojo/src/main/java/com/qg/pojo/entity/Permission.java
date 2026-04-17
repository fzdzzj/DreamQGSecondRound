package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 权限实体类
 */
@Data
public class Permission implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 权限名称
     */
    private String permName;
    /**
     * 请求方式
     */
    private String method;
    /**
     * 请求路径
     */
    private String url;
}
