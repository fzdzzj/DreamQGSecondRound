package com.qg.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qg.pojo.dto.PrivateMessageSendDTO;
import com.qg.pojo.entity.BizPrivateMessage;
import com.qg.pojo.vo.ConversationVO;
import com.qg.pojo.vo.PrivateMessageVO;

import java.util.List;

/**
 * 私聊消息服务接口
 */
public interface PrivateMessageService extends IService<BizPrivateMessage> {

    /**
     * 发送私聊消息
     *
     * @param dto 消息发送DTO
     */
    void sendMessage(PrivateMessageSendDTO dto);

    /**
     * 游标分页获取聊天记录
     *
     * @param peerId        会话ID（接收者ID）
     * @param lastMessageId 最后一条消息ID（分页游标）
     * @param pageSize      每页数量（默认10条）
     * @return 分页后的私聊消息VO列表
     */
    List<PrivateMessageVO> getHistoryByCursor(Long peerId, Long lastMessageId, Integer pageSize);

    /**
     * 将会话标记为已读
     *
     * @param peerId 会话ID（接收者ID）
     * @return
     */
    void markConversationRead(Long peerId);

    /**
     * 获取会话列表
     *
     * @return 会话VO列表
     */
    List<ConversationVO> getConversations();

    /**
     * 删除单条消息（逻辑删除）
     *
     * @param messageId 消息ID
     */
    void deleteMessage(Long messageId);

    /**
     * 删除整个会话（隐藏）
     *
     * @param peerId 会话ID（接收者ID）
     */
    void deleteConversation(Long peerId);

    /**
     * 获取当前用户总未读数
     *
     * @return 未读数
     */
    Long getUnreadCount();

    /**
     * 清空会话消息（设置清空时间戳）
     *
     * @param peerId 会话ID（接收者ID）
     */
    void clearConversation(Long peerId);


}