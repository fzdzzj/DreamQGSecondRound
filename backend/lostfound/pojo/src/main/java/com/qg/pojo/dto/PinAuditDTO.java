package com.qg.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PinAuditDTO {

    @NotNull(message = "申请ID不能为空")
    private Long requestId;

    @NotBlank(message = "审核状态不能为空")
    private String status; // APPROVED / REJECTED

    private String remark;
}
