package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "管理员统计查询DTO")
public class AdminStatisticsQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    @Past(message = "开始时间不能大于当前时间")
    private LocalDateTime startTime = LocalDateTime.now().minusDays(7);

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    @Future(message = "结束时间不能小于当前时间")
    private LocalDateTime endTime = LocalDateTime.now();
}
