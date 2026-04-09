package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话实体类
 */
@Data
@TableName("conversation")
public class Conversation implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;  // 当前用户ID
    private Long peerId;  // 对方用户ID
    private LocalDateTime lastMessageTime;  // 最后一条消息时间
    private Integer unreadCount;  // 未读消息数量
    @TableLogic
    private Boolean isDeleted;  // 是否已删除（0：未删除，1：已删除）

    // Getters and Setters
}
