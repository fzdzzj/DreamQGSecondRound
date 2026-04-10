package com.qg.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemPageQueryDTO {
    @Min(1)
    private Integer pageNum;
    @Min(1)
    @Max(100)
    private Integer pageSize;
    private String type;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String keyword;
    private String aiCategory; // 可查询子表
    private String aiStatus;
}
