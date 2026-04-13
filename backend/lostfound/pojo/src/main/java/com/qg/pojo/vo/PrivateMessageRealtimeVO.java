package com.qg.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PrivateMessageRealtimeVO implements Serializable {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private String messageType;
    private String imageUrl;
    private Integer status;
    private LocalDateTime createTime;
}
