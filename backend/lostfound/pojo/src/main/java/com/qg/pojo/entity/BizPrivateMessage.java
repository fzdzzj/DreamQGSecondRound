package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private String status;

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
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
