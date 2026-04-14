package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
@Schema(description = "更新物品DTO")
@Data
public class UpdateBizItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "标题/物品名称")
    @NotBlank(message = "标题/物品名称不能为空")
    private String title;
    @Schema(description = "详细描述")
    @NotBlank(message = "详细描述不能为空")
    private String description;
    @Schema(description = "地点")
    @NotBlank(message = "地点不能为空")
    private String location;
    @Schema(description = "丢失/拾取时间")
    @NotNull(message = "丢失/拾取时间不能为空")
    private LocalDateTime happenTime;
    @Schema(description = "联系方式")
    @NotBlank(message = "联系方式不能为空")
    private String contactMethod;
    @Schema(description = "物品状态，1：PENDING 未处理，2：MATCHED 已匹配，3：CLOSED 已关闭，4：REPORTED 已举报，5：DELETED 已删除")
    @NotNull(message = "物品状态不能为空")
    private String status;
    @Schema(description = "图片URL列表")
    private List<String> imageUrls;
}
