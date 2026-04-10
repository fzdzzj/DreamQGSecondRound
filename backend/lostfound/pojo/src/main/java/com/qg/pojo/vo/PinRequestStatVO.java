package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "置顶请求统计 VO")
public class PinRequestStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "置顶请求ID")
    private Long id;
    @Schema(description = "物品ID")
    private Long itemId;
    @Schema(description = "申请人ID")
    private Long applicantId;
    @Schema(description = "置顶请求状态描述")
    private String statusDesc;
    @Schema(description = "置顶请求状态")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
