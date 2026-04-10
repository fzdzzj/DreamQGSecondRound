package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "物品分页查询DTO")
public class ItemPageQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Min(1)
    @Schema(description = "页码")
    private Integer pageNum;
    @Min(1)
    @Max(100)
    @Schema(description = "页大小")
    private Integer pageSize;
    @Schema(description = "物品类型")
    private String type;
    @Schema(description = "物品地点")
    private String location;
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
    @Schema(description = "查询关键词")
    private String keyword;
    @Schema(description = "AI分类")
    private String aiCategory; // 可查询子表
    @Schema(description = "AI状态")
    private String aiStatus;
}
