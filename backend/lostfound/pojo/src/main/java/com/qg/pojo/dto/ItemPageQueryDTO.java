package com.qg.pojo.dto;

import com.qg.common.constant.DefaultPageConstant;
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
    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码")
    private Integer pageNum = DefaultPageConstant.DEFAULT_PAGE_NUM;
    @Min(value = 1, message = "页大小不能小于1")
    @Max(value = 100, message = "页大小不能大于100")
    @Schema(description = "页大小")
    private Integer pageSize = DefaultPageConstant.DEFAULT_PAGE_SIZE;
    @Schema(description = "物品类型 1:丢失物品 2:拾取物品", example = "1")
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
    private String aiCategory;
}
