package com.qg.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class PinAuditDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "申请ID不能为空")
    private Long requestId;

    @NotBlank(message = "审核状态不能为空")
    private String status; // APPROVED / REJECTED

    private String remark;
}
