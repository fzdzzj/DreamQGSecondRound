package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物品图片表
 */
@Data
@TableName("biz_item_image")
public class BizItemImage implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 物品ID
     * 关联物品ID（主表ID）
     */
    private Long itemId;
    /**
     * 图片地址
     */
    private String url;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}