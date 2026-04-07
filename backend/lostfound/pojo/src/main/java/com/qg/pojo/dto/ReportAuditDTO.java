package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "举报审核DTO")
public class ReportAuditDTO {

    @NotNull(message = "举报ID不能为空")
    private Long reportId;

    @NotBlank(message = "审核结果不能为空")
    private String status; // APPROVED / REJECTED

    private String remark;
}
