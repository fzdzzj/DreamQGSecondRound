package com.qg.server.websocket.model;

import com.qg.pojo.vo.PrivateMessageRealtimeVO;
import lombok.Data;
/**
 * 私聊消息载荷
 */
@Data
public class WsPrivateMessagePayload {

    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Integer messageType;
    private String imageUrl;
    private Integer status;
    private Object createTime;

    public static WsPrivateMessagePayload from(PrivateMessageRealtimeVO vo) {
        WsPrivateMessagePayload payload = new WsPrivateMessagePayload();
        payload.setId(vo.getId());
        payload.setSenderId(vo.getSenderId());
        payload.setReceiverId(vo.getReceiverId());
        payload.setContent(vo.getContent());
        payload.setMessageType(Integer.parseInt(vo.getMessageType()));
        payload.setImageUrl(vo.getImageUrl());
        payload.setStatus(vo.getStatus());
        payload.setCreateTime(vo.getCreateTime());
        return payload;
    }
}
