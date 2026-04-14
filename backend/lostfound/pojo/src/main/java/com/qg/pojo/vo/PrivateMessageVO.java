package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私聊消息 VO
 */
@Data
@Schema(description = "私聊消息 VO")
public class PrivateMessageVO implements Serializable {
    private static final long serialVersionUID = 1L;


    @Schema(description = "消息ID")
    private Long id;


    @Schema(description = "发送者ID")
    private Long senderId;


    @Schema(description = "接收者ID")
    private Long receiverId;


    @Schema(description = "消息内容")
    private String content;


    @Schema(description = "消息状态,1:已读,0:未读", example = "1")
    private String status;


    @Schema(description = "发送时间", example = "2021-01-01 00:00:00")
    private LocalDateTime createTime;
}
