package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "AI统计报告VO")
@Data
public class AdminAiStatisticsVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "AI统计报告内容")
    private String aiSummary;
    @Schema(description = "模型名字")
    private String modelName;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
