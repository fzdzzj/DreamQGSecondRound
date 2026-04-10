package com.qg.pojo.dto;

import com.qg.common.enums.ReportReasonEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "举报DTO")
public class ReportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "物品ID")
    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @Schema(description = "举报理由")
    @NotBlank(message = "举报理由不能为空,请输入1(虚假信息)/2(恶意举报)/或3(其他)")
    private String reason;

    @Schema(description = "详细说明")
    @NotBlank(message = "详细说明不能为空")
    private String detail;
}
