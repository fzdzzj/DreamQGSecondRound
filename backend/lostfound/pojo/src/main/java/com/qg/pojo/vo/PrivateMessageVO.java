package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私聊消息 VO
 */
@Data
public class PrivateMessageVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @Schema(description = "消息ID")
    private Long id;

    /**
     * 发送者ID
     */
    @Schema(description = "发送者ID")
    private Long senderId;

    /**
     * 接收者ID
     */
    @Schema(description = "接收者ID")
    private Long receiverId;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String content;

    /**
     * 消息状态
     */
    @Schema(description = "消息状态")
    private String status;

    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    private LocalDateTime createTime;
}
