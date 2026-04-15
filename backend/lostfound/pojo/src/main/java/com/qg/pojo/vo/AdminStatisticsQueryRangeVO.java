package com.qg.pojo.vo;

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

    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
