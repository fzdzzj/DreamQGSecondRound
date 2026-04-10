package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "举报详情 VO")
public class ReportDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "物品ID")
    private Long itemId;
    @Schema(description = "举报人ID")
    private Long reporterId;
    @Schema(description = "举报理由,1:虚假信息,2:恶意举报,3:其他",example="1")
    private String reason;
    @Schema(description = "举报详情")
    private String detail;
    @Schema(description = "举报状态描述",example="待处理")
    private String statusDesc;
    @Schema(description = "举报状态,1:待处理,2:已处理,3:已拒绝",example="1")
    private String status;

    private Long adminId;
    @Schema(description = "审核备注")
    private String auditRemark;
    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    private LocalDateTime createTime;
}
