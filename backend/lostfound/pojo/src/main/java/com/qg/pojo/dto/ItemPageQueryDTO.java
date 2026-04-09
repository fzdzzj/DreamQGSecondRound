package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "物品分页查询参数")
public class ItemPageQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Min(1)
    @Schema(description = "页码")
    private Integer pageNum = 1;
    @Min(1)
    @Max(50)
    private Integer pageSize = 10;
    @Schema(description = "物品类型")
    private String type; // LOST / FOUND
    @Schema(description = "搜索关键词")
    private String keyword; // 标题/描述模糊搜索
    @Schema(description = "地点")
    private String location;
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
