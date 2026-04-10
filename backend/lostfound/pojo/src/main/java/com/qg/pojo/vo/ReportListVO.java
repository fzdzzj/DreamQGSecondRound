package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "举报列表VO")
public class ReportListVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID
     */
    @Schema(description = "主键ID", example = "5")
    private Long id;
    /**
     * 举报物品ID
     */
    @Schema(description = "举报物品ID", example = "5")
    private Long itemId;
    /**
     * 举报人ID
     */
    @Schema(description = "举报人ID", example = "5")
    private Long reporterId;

    /**
     * 状态
     */
    @Schema(description = "状态,1:待处理,2:已处理,3:已拒绝", example = "PENDING")
    private String status;
    /**
     * 状态描述
     */
    @Schema(description = "状态描述", example = "待处理")
    private String statusDesc;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2021-01-01 00:00:00")
    private LocalDateTime createTime;
}
