package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "私信实时VO")
public class PrivateMessageRealtimeVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "私信ID")
    private Long id;
    @Schema(description = "发送者ID")
    private Long senderId;
    @Schema(description = "接收者ID")
    private Long receiverId;
    @Schema(description = "私信内容")
    private String content;
    @Schema(description = "消息类型 1.TEXT/2.IMAGE")
    private String messageType;
    @Schema(description = "如果是图片消息，保存图片地址")
    private String imageUrl;
    @Schema(description = "消息状态 0:未读,1:已读")
    private Integer status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
