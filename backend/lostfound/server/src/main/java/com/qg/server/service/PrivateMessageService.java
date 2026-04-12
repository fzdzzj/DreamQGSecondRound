package com.qg.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qg.common.result.PageResult;
import com.qg.pojo.dto.PrivateMessageSendDTO;
import com.qg.pojo.entity.BizPrivateMessage;
import com.qg.pojo.vo.ConversationVO;
import com.qg.pojo.vo.PrivateMessageVO;

import java.util.List;

public interface PrivateMessageService extends IService<BizPrivateMessage> {

    /**
     * 发送私聊消息
     *
     * @param sendDTO 发送参数
     */
    void sendMessage(PrivateMessageSendDTO sendDTO);

    /**
     * 获取当前用户会话列表
     *
     * @return 会话列表
     */
    List<ConversationVO> getConversationList();

    /**
     * 获取聊天记录
     *
     * @param peerId 对方用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<PrivateMessageVO> getChatHistory(Long peerId, Integer pageNum, Integer pageSize);

    /**
     * 将整个会话标记为已读
     *
     * @param peerId 对方用户ID
     */
    void markConversationAsRead(Long peerId);

    /**
     * 删除单条消息（仅当前用户视角）
     *
     * @param messageId 消息ID
     */
    void deleteMessage(Long messageId);

    /**
     * 删除会话（仅当前用户视角）
     *
     * @param peerId 对方用户ID
     */
    void deleteConversation(Long peerId);

    /**
     * 获取当前用户未读消息总数
     *
     * @return 未读消息数
     */
    Long getUnreadCount();
}
