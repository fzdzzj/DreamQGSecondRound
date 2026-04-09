package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "举报DTO")
public class ReportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @NotBlank(message = "举报理由不能为空")
    private String reason;

    @Schema(description = "详细说明")
    private String detail;
}
