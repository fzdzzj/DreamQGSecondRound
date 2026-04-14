package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
/**
 * 私聊会话表
 */
@Data
@TableName("biz_private_conversation")
public class BizPrivateConversation implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 对方用户ID
     */
    private Long peerId;

    /**
     * 用户视角下清空聊天时间
     */
    private LocalDateTime clearBeforeTime;

    /**
     * 最后一条已读消息ID，可选
     */
    private Long lastReadMessageId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
