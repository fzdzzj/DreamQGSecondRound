package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话列表 VO
 */
@Data
@Schema(description = "会话列表 VO")
public class ConversationVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 对方用户ID
     */
    @Schema(description = "对方用户ID")
    private Long peerId;

    /**
     * 对方昵称
     */
    @Schema(description = "对方昵称")
    private String peerNickname;

    /**
     * 对方头像
     */
    @Schema(description = "对方头像")
    private String peerAvatar;

    /**
     * 最后一条消息内容
     */
    @Schema(description = "最后消息内容")
    private String lastMessage;

    /**
     * 最后一条消息时间
     */
    @Schema(description = "最后消息时间")
    private LocalDateTime lastMessageTime;

    /**
     * 未读消息数
     */
    @Schema(description = "未读消息数")
    private Long unreadCount;
}
