package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_private_conversation")
public class BizPrivateConversation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
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
