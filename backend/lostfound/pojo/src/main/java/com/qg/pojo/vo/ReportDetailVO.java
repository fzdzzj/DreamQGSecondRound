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
    @Schema(description = "举报理由",example="1(虚假信息)")
    private String reason;
    @Schema(description = "举报详情")
    private String detail;

    private String statusDesc;
    private String status;

    private Long adminId;
    private String auditRemark;
    private LocalDateTime auditTime;

    private LocalDateTime createTime;
}
