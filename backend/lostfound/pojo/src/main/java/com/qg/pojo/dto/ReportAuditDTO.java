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

    @NotNull(message = "举报ID不能为空")
    private Long reportId;

    @NotBlank(message = "审核结果不能为空")
    private String status; // APPROVED / REJECTED

    private String remark;
}
