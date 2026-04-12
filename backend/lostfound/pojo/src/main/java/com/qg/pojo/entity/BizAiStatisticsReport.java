package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_ai_statistics_report")
public class BizAiStatisticsReport implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate statDate;
    private String statType;
    private String sourceDataJson;
    private String aiSummary;
    private String modelName;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
