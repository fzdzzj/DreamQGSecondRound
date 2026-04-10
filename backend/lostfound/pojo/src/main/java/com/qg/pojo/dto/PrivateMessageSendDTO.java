package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 私聊发送 DTO
 */
@Data
@Schema(description = "私聊发送 DTO")
public class PrivateMessageSendDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 接收者ID
     */
    @Schema(description = "接收者ID")
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    @NotBlank(message = "消息内容不能为空")
    private String content;
}
