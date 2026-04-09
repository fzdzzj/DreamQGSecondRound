
package com.qg.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class PinApplyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @NotBlank(message = "申请理由不能为空")
    private String reason;
}
