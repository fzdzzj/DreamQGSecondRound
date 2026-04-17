package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "认领申请审批DTO")
public class ApproveRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "更新状态")
    private String status;
    @Schema(description = "备注")
    private String remark;
}
