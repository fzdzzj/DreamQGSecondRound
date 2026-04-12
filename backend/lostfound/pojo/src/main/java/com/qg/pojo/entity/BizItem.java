package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("biz_item")
public class BizItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 最新的 AI 结果 ID，指向子表 */
    private Long currentAiResultId;

    private Long userId;
    private String type; // LOST / FOUND
    private String title;
    private String description;
    private String location;
    private LocalDateTime happenTime;
    private String status; // OPEN, MATCHED, CLOSED...
    private Integer isPinned;
    private LocalDateTime pinExpireTime;
    private String contactMethod;
    private String normalizedLocation;
    private String aiStatus; // PENDING, SUCCESS, FAILURE
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
