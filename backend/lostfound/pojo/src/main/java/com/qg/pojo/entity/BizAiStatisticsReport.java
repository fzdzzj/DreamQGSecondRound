package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 统计报告表
 */
@Data
@TableName("biz_ai_statistics_report")
public class BizAiStatisticsReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 统计日期
     */
    private LocalDate statDate;
    /**
     * 统计类型
     */
    private String statType;
    /**
     * 源数据JSON
     */
    private String sourceDataJson;
    /**
     * AI摘要
     */
    private String aiSummary;
    /**
     * AI模型名称
     */
    private String modelName;
    /**
     * 状态
     */
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
