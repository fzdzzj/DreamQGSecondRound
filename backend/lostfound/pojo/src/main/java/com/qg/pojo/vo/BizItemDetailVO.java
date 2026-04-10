package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "丢失物品详情")
public class BizItemDetailVO implements Serializable {
    @Schema(description = "物品ID")
    private Long id;
    @Schema(description = "物品类型")
    private String type;
    @Schema(description = "物品标题")
    private String title;
    @Schema(description = "物品描述")
    private String description;
    @Schema(description = "物品位置")
    private String location;
    @Schema(description = "物品发生时间")
    private LocalDateTime happenTime;
    @Schema(description = "物品状态")
    private String status;
    @Schema(description = "物品状态描述")
    private String statusDesc;
    @Schema(description = "物品图片URL列表")
    private List<String> imageUrls;
    @Schema(description = "物品AI状态")
    private String aiStatus;
    @Schema(description = "物品AI分类")
    private String aiCategory;
    @Schema(description = "物品AIT标签")
    private String aiTags;
    @Schema(description = "物品AI描述")
    private String aiDescription;
}
