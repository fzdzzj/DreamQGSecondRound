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
    /**
     * 当前用户ID
     */
    private Long userId;
    /**
     * 对方用户ID
     */
    private Long peerId;
    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;
    /**
     * 未读消息数量
     */
    private Integer unreadCount;
    /**
     * 是否已删除（0：未删除，1：已删除）
     */
    @TableLogic
    private Boolean isDeleted;
}
