package com.qg.pojo.dto;

import com.qg.common.constant.DefaultPageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Schema(description = "管理员AI统计查询DTO")
public class AdminAiStatisticsQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "统计日期")
    private LocalDate statDate;
    @Schema(description = "统计类型,1日,2周,3月")
    private String statType;
    @Schema(description = "页码")
    private Integer pageNum = DefaultPageConstant.DEFAULT_PAGE_NUM;
    @Schema(description = "每页数量")
    private Integer pageSize = DefaultPageConstant.DEFAULT_PAGE_SIZE;
}
