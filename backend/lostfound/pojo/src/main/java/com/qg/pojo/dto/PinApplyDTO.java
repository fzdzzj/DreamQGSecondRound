
package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "物品置顶申请DTO")
public class PinApplyDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "物品ID")
    private Long itemId;
    @Schema(description = "申请理由")
    @NotBlank(message = "申请理由不能为空")
    private String reason;
}
