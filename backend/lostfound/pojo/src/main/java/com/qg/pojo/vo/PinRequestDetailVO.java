package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "置顶请求详情")
public class PinRequestDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "物品ID")
    private Long itemId;
    @Schema(description = "申请人ID")
    private Long applicantId;

    @Schema(description = "申请理由")
    private String reason;
    @Schema(description = "状态描述")
    private String statusDesc;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "审核管理员ID")
    private Long auditAdminId;
    @Schema(description = "审核备注")
    private String auditRemark;
    @Schema(description = "审核时间")
    private LocalDateTime auditTime;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
