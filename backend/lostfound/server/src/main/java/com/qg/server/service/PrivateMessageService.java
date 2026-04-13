package com.qg.server.service;

import com.qg.pojo.dto.PrivateMessageSendDTO;
import com.qg.pojo.vo.ConversationVO;
import com.qg.pojo.vo.PrivateMessageRealtimeVO;
import com.qg.pojo.vo.PrivateMessageVO;

import java.util.List;

public interface PrivateMessageService {

    PrivateMessageRealtimeVO sendMessage(PrivateMessageSendDTO sendDTO);

    List<PrivateMessageVO> getChatHistoryByCursor(Long peerId, Long lastMessageId, Integer pageSize);

    void markConversationAsRead(Long peerId);

    void clearConversation(Long peerId);

    List<ConversationVO> getConversationList();

    Long getUnreadCount();

    void deleteConversation(Long id);

    void deleteMessage(Long id);
}
