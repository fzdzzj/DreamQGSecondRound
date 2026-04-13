package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私聊消息实体
 */
@Data
@TableName("biz_private_message")
public class BizPrivateMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息状态：UNREAD / READ
     */
    private Integer status;

    /**
     * 消息类型：TEXT / IMAGE
     */
    private String messageType;
    /**
     * 图片URL
     */
    private String imageUrl;
    /**
     * 客户端消息ID
     */
    private String clientMsgId;

    /**
     * 发送方是否删除：0否 1是
     */
    private Integer senderDeleted;

    /**
     * 接收方是否删除：0否 1是
     */
    private Integer receiverDeleted;

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
