
package com.qg.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PinApplyDTO {

    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @NotBlank(message = "申请理由不能为空")
    private String reason;
}
