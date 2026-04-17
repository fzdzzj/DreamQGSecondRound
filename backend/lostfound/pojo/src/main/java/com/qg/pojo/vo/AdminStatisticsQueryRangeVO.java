package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员统计查询范围VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminStatisticsQueryRangeVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
