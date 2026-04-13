package com.qg.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversationVO {
    private Long peerId;                  // 对方用户ID
    private String peerNickname;          // 对方昵称
    private String peerAvatar;            // 对方头像
    private String lastMessage;           // 最后一条消息内容
    private String lastMessageType;       // 消息类型 TEXT/IMAGE
    private String lastImageUrl;          // 如果是图片消息，保存图片地址
    private LocalDateTime lastMessageTime;// 最后一条消息时间
    private Long unreadCount;             // 未读消息数
}
