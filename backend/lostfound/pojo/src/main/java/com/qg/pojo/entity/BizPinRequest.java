package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.qg.common.enums.PinRequestStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bin_pin_request")
public class BizPinRequest {

    @TableId(type= IdType.AUTO)
    private Long id;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 申请人id
     */
    private Long applicantId;
    /**
     * 申请原因
     */
    private String reason;

    /**
     * 状态：PENDING / APPROVED / REJECTED / CANCELED
     */
    private String status;

    /**
     * 审核管理员ID
     */
    private Long auditAdminId;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    @TableField(fill= FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill= FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
