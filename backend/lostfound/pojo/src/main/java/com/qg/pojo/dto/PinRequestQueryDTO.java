package com.qg.pojo.dto;

import com.qg.common.constant.DefaultPageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "置顶申请查询参数DTO")
public class PinRequestQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码", example = "1")
    private Integer pageNum = DefaultPageConstant.DEFAULT_PAGE_NUM;

    @Min(value = 1, message = "每页记录数不能小于1")
    @Max(value = 50, message = "每页记录数不能大于50")
    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize = DefaultPageConstant.DEFAULT_PAGE_SIZE;

    @Schema(description = "申请状态，1.PENDING / 2.APPROVED / 3.REJECTED/4.CANCELLED", example = "PENDING")
    private String status;

    @Schema(description = "申请用户ID")
    private Long applicantId;

    @Schema(description = "物品ID")
    private Long itemId;
}
