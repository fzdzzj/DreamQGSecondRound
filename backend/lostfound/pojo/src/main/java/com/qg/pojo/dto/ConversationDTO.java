package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 会话查询的 DTO
 */
@Data
@Schema(description = "会话查询DTO")
public class ConversationDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "当前用户ID")
    private Long userId;  // 当前用户ID
    @Schema(description = "对方用户ID")
    @NotNull(message = "对方用户ID不能为空")
    private Long peerId;  // 对方用户ID
}
