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

    private Long itemId;       // 关联物品
    private Long applicantId;  // 申请人（失主）
    private Long ownerId;      // 物品发布者（拾取者）
    private String verificationAnswer; // 核验答案
    private String status;     // PENDING / APPROVED / REJECTED / MORE_INFO_REQUIRED
    private String pickupCode; // 一次性取件码（审批通过生成）
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
