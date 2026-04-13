package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 聊天历史记录表 实体类
 * 对应数据库表：ai_chat_history
 */
@Data
@TableName("ai_chat_history")
public class AiChatHistory {

    /**
     * 主键 ID，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 会话 ID
     */
    private String chatId;

    /**
     * 类型
     */
    private String type;

    /**
     * 角色：user / assistant / system
     */
    private String role;

    /**
     * 聊天内容
     */
    private String content;

    /**
     * 创建时间，数据库默认当前时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}