package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "风险事件DTO")
public class RiskHandleDTO {
    @Schema(description = "风险事件ID")
    @NotNull(message = "风险事件ID不能为空")
    private Long riskEventId;
    @Schema(description = "处理状态")
    @NotBlank(message = "处理状态不能为空")
    private String handleStatus;
    @Schema(description = "处理备注")
    @NotBlank(message = "处理备注不能为空")
    private String handleRemark;
}
