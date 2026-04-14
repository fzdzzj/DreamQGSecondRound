package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 风险事件表
 */
@Data
@TableName("biz_risk_event")
public class BizRiskEvent {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 风险类型
     * 1=HIGH_VALUE_ITEM 高价值物品
     * 2=PERIODIC_CLUSTER 时段聚集
     * 3=SENSITIVE_ITEM 敏感物品
     * 4=ITEM_FOUND 物品已经找回
     */
    private String riskType;

    /**
     * 风险等级
     * 0=NO 无风险
     * 1=LOW 低
     * 2=MEDIUM 中
     * 3=HIGH 高
     * 4=CRITICAL 极严重
     */
    private String riskLevel;

    /**
     * 风险标题
     */
    private String title;

    /**
     * 风险内容详情
     */
    private String content;

    /**
     * 关联物品ID
     */
    private Long relatedItemId;

    /**
     * 关联用户ID
     */
    private Long relatedUserId;

    /**
     * 发生地点
     */
    private String location;

    /**
     * 发生时间窗口 如 18:30-19:00
     */
    private String timeWindow;

    /**
     * 证据JSON（图片/记录/依据）
     */
    private String evidenceJson;

    /**
     * 通知状态
     * 1=PENDING 待通知
     * 2=SUCCESS 通知成功
     * 3=FAIL 通知失败
     */
    private String notifyStatus;

    /**
     * 处理状态
     * 1=UNHANDLED 未处理
     * 2=RESOLVED 已解决
     * 3=IGNORED 已忽略
     */
    private String handleStatus;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 处理人ID
     */
    private Long handledBy;

    /**
     * 处理时间
     */
    private LocalDateTime handledTime;

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