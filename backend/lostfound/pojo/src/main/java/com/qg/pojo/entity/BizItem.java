package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * 物品表
 */
@Data
@TableName("biz_item")
public class BizItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 最新的 AI 结果 ID，指向子表
     */
    private Long currentAiResultId;

    /**
     * 创建者用户 ID
     */
    private Long userId;
    /**
     * 物品类型 1:丢失物品 2:拾取物品
     */
    private String type;
    /**
     * 标题/物品名称
     */
    private String title;
    /**
     * 详细描述
     */
    private String description;
    /**
     * 地点
     */
    private String location;
    /**
     * 丢失/拾取时间
     */
    private LocalDateTime happenTime;
    /**
     * 物品状态，1：PENDING 未处理，2：MATCHED 已匹配，3：CLOSED 已关闭，4：REPORTED 已举报，5：DELETED 已删除
     */
    private String status; // OPEN, MATCHED, CLOSED...
    /**
     * 置顶状态，1：已置顶，0：未置顶
     */
    private Integer isPinned;
    /**
     * 置顶到期时间
     */
    private LocalDateTime pinExpireTime;
    /**
     * 联系方式
     */
    private String contactMethod;
    /**
     * AI 状态，1：PENDING 等待处理，2：SUCCESS 处理成功，3：FAILURE 处理失败
     */
    private String aiStatus;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
