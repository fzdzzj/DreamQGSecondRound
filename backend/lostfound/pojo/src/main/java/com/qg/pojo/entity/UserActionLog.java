package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户行为日志表 实体类
 * 对应数据库表：user_action_log
 */
@Data
@TableName("user_action_log")
public class UserActionLog {

    /**
     * 主键 ID
     * 对应字段：id，自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     * 对应字段：user_id，非空
     */
    private Long userId;

    /**
     * 操作类型
     * 对应字段：action_type，非空
     * 1：CLAIM / 2：EDIT_POST / 3.POST
     */
    private String actionType;

    /**
     * 创建时间
     * 对应字段：create_time，默认当前时间戳
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}