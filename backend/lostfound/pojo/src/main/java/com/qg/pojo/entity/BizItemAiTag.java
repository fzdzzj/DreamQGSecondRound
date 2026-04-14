package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物品AI标签表
 */
@Data
@TableName("biz_item_ai_tag")
public class BizItemAiTag implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    /**
     * 物品ID
     */
    private Long itemId;
    /**
     * AI结果版本
     */
    private Integer aiResultVersion;

    /**
     * AI标签
     */
    @TableField("ai_tags")
    private String aiTags;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
