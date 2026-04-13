package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "认领申请审批DTO")
public class ApproveRequestDTO {
    @Schema(description = "认领申请ID")
    private Long requestId;
    @Schema(description = "更新状态")
    private String status;
    @Schema(description = "备注")
    private String remark;
}
