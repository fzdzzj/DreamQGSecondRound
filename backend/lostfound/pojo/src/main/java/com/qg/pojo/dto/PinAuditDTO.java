package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "物品置顶审核DTO")
public class PinAuditDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "申请ID")
    @NotNull(message = "申请ID不能为空")
    private Long requestId;
    @Schema(description = "审核状态")
    @NotBlank(message = "审核状态不能为空，1.PENDING / 2.APPROVED / 3.REJECTED/4.CANCELLED")
    private String status;
    @Schema(description = "审核备注")
    @NotBlank(message = "审核备注不能为空")
    private String remark;
}
