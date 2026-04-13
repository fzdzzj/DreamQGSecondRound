package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "会话VO")
public class ConversationVO {
    @Schema(description = "对方用户ID")
    private Long peerId;                  // 对方用户ID
    @Schema(description = "对方昵称")
    private String peerNickname;          // 对方昵称
    @Schema(description = "对方头像")
    private String peerAvatar;            // 对方头像
    @Schema(description = "最后一条消息内容")
    private String lastMessage;           // 最后一条消息内容
    @Schema(description = "消息类型 TEXT/IMAGE")
    private String lastMessageType;       // 消息类型 TEXT/IMAGE
    @Schema(description = "如果是图片消息，保存图片地址")
    private String lastImageUrl;          // 如果是图片消息，保存图片地址
    @Schema(description = "最后一条消息时间")
    private LocalDateTime lastMessageTime;// 最后一条消息时间
    @Schema(description = "未读消息数")
    private Long unreadCount;             // 未读消息数
}
