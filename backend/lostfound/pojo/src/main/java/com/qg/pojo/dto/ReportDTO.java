package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "举报DTO")
public class ReportDTO {

    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @NotBlank(message = "举报理由不能为空")
    private String reason;

    @Schema(description = "详细说明")
    private String detail;
}
