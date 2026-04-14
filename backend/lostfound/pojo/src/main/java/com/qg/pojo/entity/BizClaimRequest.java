package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 认领申请表，双向审批
 */
@Data
@TableName("biz_claim_request")
public class BizClaimRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 关联物品ID
     */
    private Long itemId;
    private Long applicantId;
    /**
     * 物品发布者ID
     */
    private Long ownerId;
    /**
     * 核验答案
     */
    private String verificationAnswer;
    /**
     * 状态
     * 1:待审批，2:审批通过，3:审批拒绝，4:需要更多信息审批
     */
    private String status;
    /**
     * 一次性取件码（审批通过生成）
     */
    private String pickupCode;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
