package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "物品统计VO")
public class BizItemStatVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "物品ID")
    private Long id;
    @Schema(description = "物品标题")
    private String title;
    @Schema(description = "物品位置")
    private String location;
    @Schema(description = "物品发生时间")
    private LocalDateTime happenTime;
    @Schema(description = "物品状态：1：丢失 2：拾取")
    private String status;
    @Schema(description = "物品状态描述")
    private String statusDesc;
    @Schema(description = "物品描述")
    private String description;
    @Schema(description = "物品AI分类")
    private String aiCategory;
}
