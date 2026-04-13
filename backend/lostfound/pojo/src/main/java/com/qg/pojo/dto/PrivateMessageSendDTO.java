package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "发送私聊消息DTO")
public class PrivateMessageSendDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "接收者ID不能为空")
    @Schema(description = "接收者ID")
    private Long receiverId;

    @Schema(description = "文本内容")
    private String content;

    @Schema(description = "消息类型：1文本消息/2图片消息")
    private String messageType;

    @Schema(description = "图片地址")
    private String imageUrl;

    @Schema(description = "客户端消息ID，防重")
    private String clientMsgId;
}
