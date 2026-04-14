package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "举报审核DTO")
public class ReportAuditDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "举报ID")
    @NotNull(message = "举报ID不能为空")
    private Long reportId;

    @Schema(description = "审核状态")
    @NotBlank(message = "审核状态不能为空 1.PENDING / 2.APPROVED / 3.REJECTED")
    private String status;
    @Schema(description = "审核备注")
    @NotBlank(message = "审核备注不能为空")
    private String remark;
}
