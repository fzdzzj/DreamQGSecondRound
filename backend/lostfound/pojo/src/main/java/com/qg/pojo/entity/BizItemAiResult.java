package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物品AI结果表
 */
@Data
public class BizItemAiResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 结果版本号
     */
    private Integer resultVersion;

    /**
     * 来源类型（AUTO/REGENERATE）
     */
    private String sourceType;

    /**
     * AI请求prompt
     */
    private String promptText;

    /**
     * 用户原始描述
     */
    private String originText;

    /**
     * AI生成描述
     */
    private String aiDescription;

    /**
     * AI分类
     */
    private String aiCategory;

    /**
     * AI标签
     */
    private String aiTags;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 状态（PENDING/SUCCESS/FAILED）
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 是否删除（0否 1是）
     */
    private Integer isDeleted;

    private Long createUser;

    private Long updateUser;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
