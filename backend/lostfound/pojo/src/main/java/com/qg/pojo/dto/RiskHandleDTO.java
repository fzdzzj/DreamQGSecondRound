package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "风险事件DTO")
public class RiskHandleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "处理状态")
    @NotBlank(message = "处理状态不能为空")
    private String handleStatus;
    @Schema(description = "处理备注")
    @NotBlank(message = "处理备注不能为空")
    private String handleRemark;
}
