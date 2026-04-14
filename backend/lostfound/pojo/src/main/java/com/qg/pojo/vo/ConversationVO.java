package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "会话VO")
public class ConversationVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "对方用户ID")
    private Long peerId;
    @Schema(description = "对方昵称")
    private String peerNickname;
    @Schema(description = "对方头像")
    private String peerAvatar;
    @Schema(description = "最后一条消息内容")
    private String lastMessage;
    @Schema(description = "消息类型 1.TEXT/2.IMAGE")
    private String lastMessageType;
    @Schema(description = "如果是图片消息，保存图片地址")
    private String lastImageUrl;
    @Schema(description = "最后一条消息时间")
    private LocalDateTime lastMessageTime;
    @Schema(description = "未读消息数")
    private Long unreadCount;
}
