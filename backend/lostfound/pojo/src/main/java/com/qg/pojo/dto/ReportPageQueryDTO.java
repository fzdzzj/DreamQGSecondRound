package com.qg.pojo.dto;

import com.qg.common.constant.DefaultPageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "管理员查询举报申请分页参数")
public class ReportPageQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = DefaultPageConstant.DEFAULT_PAGE_NUM;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能大于100")
    private Integer pageSize = DefaultPageConstant.DEFAULT_PAGE_SIZE;

    @Schema(description = "举报状态: PENDING/APPROVED/REJECTED,1:待审核,2:已审核,3:已拒绝")
    private String status;

    @Schema(description = "举报开始时间")
    @Past(message = "开始时间不能大于当前时间")
    private LocalDateTime startTime;

    @Schema(description = "举报结束时间")
    @Past(message = "结束时间不能大于当前时间")
    private LocalDateTime endTime;

    @Schema(description = "举报人ID")
    private Long reporterId;
    @Schema(description = "物品ID")

    private Long itemId;
}
