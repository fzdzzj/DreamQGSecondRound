package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 置顶申请表
 */
@Data
@TableName("biz_pin_request")
public class BizPinRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
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
     * 状态：1.待审核 2.已审核 3.已拒绝 4.已取消
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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
