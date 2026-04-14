package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 举报记录表
 * 对应表：biz_report
 */
@Data
@TableName("biz_report")
public class BizReport implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 被举报物品ID
     */
    private Long itemId;

    /**
     * 举报人ID
     */
    private Long reporterId;

    /**
     * 举报理由: FAKE_INFO, MALICIOUS, OTHER
     */
    private String reason;

    /**
     * 详细说明
     */
    private String detail;

    /**
     * 状态: 1.PENDING, 2.APPROVED, 3.REJECTED
     */
    private String status;

    /**
     * 审核管理员ID
     */
    private Long adminId;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}