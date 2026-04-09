package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "管理员查询举报申请分页参数")
public class ReportPageQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "举报状态: PENDING/APPROVED/REJECTED")
    private String status;

    @Schema(description = "举报开始时间")
    private LocalDateTime startTime;

    @Schema(description = "举报结束时间")
    private LocalDateTime endTime;

    @Schema(description = "举报人ID")
    private Long reporterId;
}
