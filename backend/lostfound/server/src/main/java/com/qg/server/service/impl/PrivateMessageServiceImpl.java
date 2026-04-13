package com.qg.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.MessageTypeConstant;
import com.qg.common.constant.ReadStatusConstant;
import com.qg.common.context.BaseContext;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.BaseException;
import com.qg.pojo.dto.PrivateMessageSendDTO;
import com.qg.pojo.entity.Conversation;
import com.qg.pojo.vo.ConversationVO;
import com.qg.pojo.vo.PrivateMessageRealtimeVO;
import com.qg.pojo.vo.PrivateMessageVO;
import com.qg.pojo.entity.BizPrivateConversation;
import com.qg.pojo.entity.BizPrivateMessage;
import com.qg.pojo.entity.SysUser;
import com.qg.server.mapper.BizPrivateConversationDao;
import com.qg.server.mapper.BizPrivateMessageDao;
import com.qg.server.mapper.UserDao;
import com.qg.server.service.PrivateMessageService;
import com.qg.server.websocket.PrivateChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrivateMessageServiceImpl
        extends ServiceImpl<BizPrivateMessageDao, BizPrivateMessage>
        implements PrivateMessageService {

    private final BizPrivateMessageDao messageDao;
    private final BizPrivateConversationDao conversationDao;
    private final UserDao userDao;
    private final PrivateChatWebSocketHandler wsHandler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrivateMessageRealtimeVO sendMessage(PrivateMessageSendDTO sendDTO) {
        Long senderId = BaseContext.getCurrentId();
        Long receiverId = sendDTO.getReceiverId();

        // 验证接收者
        SysUser receiver = userDao.selectById(receiverId);
        if (receiver == null) throw new AbsentException("接收用户不存在");

        // 验证消息内容
        String messageType = sendDTO.getMessageType();
        if (MessageTypeConstant.TEXT.equals(messageType) && (sendDTO.getContent() == null || sendDTO.getContent().trim().isEmpty())) {
            throw new BaseException(400, "文本消息不能为空");
        }
        if (MessageTypeConstant.IMAGE.equals(messageType) && (sendDTO.getImageUrl() == null || sendDTO.getImageUrl().trim().isEmpty())) {
            throw new BaseException(400, "图片地址不能为空");
        }

        // 保存消息
        BizPrivateMessage message = new BizPrivateMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(sendDTO.getContent());
        message.setMessageType(messageType);
        message.setImageUrl(sendDTO.getImageUrl());
        message.setStatus(ReadStatusConstant.UNREAD);
        message.setClientMsgId(sendDTO.getClientMsgId());
        message.setSenderDeleted(0);
        message.setReceiverDeleted(0);
        save(message);

        // 构建前端实时消息对象
        PrivateMessageRealtimeVO vo = new PrivateMessageRealtimeVO();
        vo.setId(message.getId());
        vo.setSenderId(senderId);
        vo.setReceiverId(receiverId);
        vo.setContent(message.getContent());
        vo.setMessageType(message.getMessageType());
        vo.setImageUrl(message.getImageUrl());
        vo.setStatus(message.getStatus());
        vo.setCreateTime(message.getCreateTime());

        // 推送给接收者
        wsHandler.sendToUser(receiverId, vo);

        // --- 更新发送者视角的会话 ---
        BizPrivateConversation senderConversation = conversationDao.selectByUserIdAndPeerId(senderId, receiverId);
        if (senderConversation == null) {
            senderConversation = new BizPrivateConversation();
            senderConversation.setUserId(senderId);
            senderConversation.setPeerId(receiverId);
            // 不要初始化 clearBeforeTime
            senderConversation.setLastReadMessageId(message.getId());
            conversationDao.insert(senderConversation);
        } else {
            senderConversation.setLastReadMessageId(message.getId());
            conversationDao.updateById(senderConversation);
        }

        // --- 确保接收者视角的会话存在（但不更新 lastReadMessageId） ---
        BizPrivateConversation receiverConversation = conversationDao.selectByUserIdAndPeerId(receiverId, senderId);
        if (receiverConversation == null) {
            receiverConversation = new BizPrivateConversation();
            receiverConversation.setUserId(receiverId);
            receiverConversation.setPeerId(senderId);
            // 不要设置 lastReadMessageId
            conversationDao.insert(receiverConversation);
        }

        return vo;
    }


    @Override
    public List<PrivateMessageVO> getChatHistoryByCursor(Long peerId, Long lastMessageId, Integer pageSize) {
        Long currentUserId = BaseContext.getCurrentId();

        SysUser peer = userDao.selectById(peerId);
        if (peer == null) throw new AbsentException("对方用户不存在");

        BizPrivateConversation conversation = conversationDao.selectByUserIdAndPeerId(currentUserId, peerId);
        LocalDateTime clearBefore = conversation != null ? conversation.getClearBeforeTime() : null;

        List<PrivateMessageVO> list = messageDao.selectChatHistoryByCursor(currentUserId, peerId, clearBefore, lastMessageId, pageSize);
        Collections.reverse(list);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markConversationAsRead(Long peerId) {
        Long currentUserId = BaseContext.getCurrentId();
        messageDao.updateStatusToRead(peerId, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearConversation(Long peerId) {
        Long currentUserId = BaseContext.getCurrentId();
        BizPrivateConversation conversation = conversationDao.selectByUserIdAndPeerId(currentUserId, peerId);
        if (conversation == null) {
            conversation = new BizPrivateConversation();
            conversation.setUserId(currentUserId);
            conversation.setPeerId(peerId);
            conversation.setClearBeforeTime(LocalDateTime.now());
            conversationDao.insert(conversation);
        } else {
            conversation.setClearBeforeTime(LocalDateTime.now());
            conversationDao.updateById(conversation);
        }
    }

    @Override
    public List<ConversationVO> getConversationList() {
        Long currentUserId = BaseContext.getCurrentId();
        List<ConversationVO> list = messageDao.selectConversationList(currentUserId);
        return list;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId) {
        Long currentUserId = BaseContext.getCurrentId();

        BizPrivateMessage message = getById(messageId);
        if (message == null) {
            throw new AbsentException("消息不存在");
        }

        if (!currentUserId.equals(message.getSenderId()) && !currentUserId.equals(message.getReceiverId())) {
            throw new BaseException(403, "无权删除该消息");
        }

        if (currentUserId.equals(message.getSenderId())) {
            message.setSenderDeleted(1);
        } else {
            message.setReceiverDeleted(1);
        }

        updateById(message);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long peerId) {
        Long currentUserId = BaseContext.getCurrentId();

        // 发送方视角
        lambdaUpdate()
                .eq(BizPrivateMessage::getSenderId, currentUserId)
                .eq(BizPrivateMessage::getReceiverId, peerId)
                .set(BizPrivateMessage::getSenderDeleted, 1)
                .update();

        // 接收方视角
        lambdaUpdate()
                .eq(BizPrivateMessage::getSenderId, peerId)
                .eq(BizPrivateMessage::getReceiverId, currentUserId)
                .set(BizPrivateMessage::getReceiverDeleted, 1)
                .update();
    }
    @Override
    public Long getUnreadCount() {
        Long currentUserId = BaseContext.getCurrentId();

        Long count = lambdaQuery()
                .eq(BizPrivateMessage::getReceiverId, currentUserId)
                .eq(BizPrivateMessage::getStatus, 0) // 0 = UNREAD
                .eq(BizPrivateMessage::getReceiverDeleted, 0)
                .count();

        return count;
    }




}
