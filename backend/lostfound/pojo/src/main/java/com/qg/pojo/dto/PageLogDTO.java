package com.qg.pojo.dto;

import com.qg.common.constant.DefaultPageConstant;
import com.qg.common.enums.OperationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "分页查询操作日志DTO")
@Data
public class PageLogDTO implements Serializable {
    @Schema(description = "页码")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum= DefaultPageConstant.DEFAULT_PAGE_NUM;
    @Schema(description = "每页数量")
    @Min(value = 1, message = "每页数量不能小于1")
    private Integer pageSize= DefaultPageConstant.DEFAULT_PAGE_SIZE;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "操作类型")
    private String operationType;
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
