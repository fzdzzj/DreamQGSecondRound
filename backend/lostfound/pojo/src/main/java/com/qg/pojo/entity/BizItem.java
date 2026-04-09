package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.qg.common.enums.BizItemStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 失物/拾物信息表
 * 对应表：biz_item
 */
@Data
@TableName("biz_item")
public class BizItem implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 最新的AI结果ID
     */
    private Long currentAiResultId;


    /**
     * 发布者ID
     */
    private Long userId;

    /**
     * 类型: LOST(丢失), FOUND(拾取)
     */
    private String type;

    /**
     * 标题/物品名称
     */
    private String title;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 地点
     */
    private String location;

    /**
     * 丢失/拾取时间
     */
    private LocalDateTime happenTime;

    /**
     * 状态: OPEN, MATCHED, CLOSED, REPORTED, DELETED
     */
    private String status;

    /**
     * 是否置顶: 0否 1是
     */
    private Integer isPinned;

    /**
     * 置顶过期时间
     */
    private LocalDateTime pinExpireTime;

    /**
     * 联系方式
     */
    private String contactMethod;

    /**
     * AI识别分类
     */
    private String aiCategory;

    /**
     * AI提取标签: ["黑色","塑料壳","操场"]
     */
    private String aiTags;

    /**
     * AI处理状态: PENDING, SUCCESS, FAILED
     */
    private String aiStatus;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}