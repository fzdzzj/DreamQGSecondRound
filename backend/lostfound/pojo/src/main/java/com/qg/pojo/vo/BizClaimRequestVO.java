package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "物品申领请求VO")
@Data
public class BizClaimRequestVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "领领请求ID")
    private Long id;
    @Schema(description = "物品ID")
    private Long itemId;
    @Schema(description = "申请人ID")
    private Long applicantId;
    @Schema(description = "验证答案")
    private String verificationAnswer;
    @Schema(description = "状态：1：待审核 2：已通过 3：已拒绝")
    private String status;
    @Schema(description = "取货码")
    private String pickupCode;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
