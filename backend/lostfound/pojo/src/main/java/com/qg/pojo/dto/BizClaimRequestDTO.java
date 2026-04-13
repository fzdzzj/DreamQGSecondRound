package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@Schema(description = "认领申请DTO")
public class BizClaimRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "物品ID")
    private Long itemId;
    @Schema(description = "申请人ID")
    private Long applicantId;
    @Schema(description = "验证答案")
    private String verificationAnswer;
}
