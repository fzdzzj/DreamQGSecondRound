package com.qg.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.DeletedConstant;
import com.qg.common.constant.MessageTypeConstant;
import com.qg.common.constant.ReadStatusConstant;
import com.qg.common.context.BaseContext;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.BaseException;
import com.qg.pojo.dto.PrivateMessageSendDTO;
import com.qg.pojo.entity.BizPrivateConversation;
import com.qg.pojo.entity.BizPrivateMessage;
import com.qg.pojo.entity.SysUser;
import com.qg.pojo.vo.ConversationVO;
import com.qg.pojo.vo.PrivateMessageRealtimeVO;
import com.qg.pojo.vo.PrivateMessageVO;
import com.qg.server.mapper.BizPrivateConversationDao;
import com.qg.server.mapper.BizPrivateMessageDao;
import com.qg.server.mapper.UserDao;
import com.qg.server.service.PrivateMessageService;
import com.qg.server.websocket.PrivateChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 私信消息服务实现
 */
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

    private final ApplicationContext applicationContext;
    private PrivateMessageServiceImpl getSelf() {
        return applicationContext.getBean(PrivateMessageServiceImpl.class);
    }
    /**
     * 发送私信消息
     *
     * @param sendDTO
     * @return
     * @return 私信消息实时VO
     * @throws AbsentException 接收用户不存在
     *                         1. 验证用户是否存在
     *                         2. 验证消息内容是否为空
     *                         3. 核心事务（仅DB）
     *                         4. 推送消息
     */
    @Override
    public PrivateMessageRealtimeVO sendMessage(PrivateMessageSendDTO sendDTO) {
        Long senderId = BaseContext.getCurrentId();
        Long receiverId = sendDTO.getReceiverId();

        // 1. 验证用户
        SysUser receiver = userDao.selectById(receiverId);
        if (receiver == null) {
            log.warn("接收用户不存在，ID: {}", receiverId);
            throw new AbsentException("接收用户不存在");
        }
        // 2. 验证消息内容
        String messageType = sendDTO.getMessageType();
        if (MessageTypeConstant.TEXT.equals(messageType) && (sendDTO.getContent() == null || sendDTO.getContent().trim().isEmpty())) {
            log.warn("文本消息不能为空");
            throw new BaseException(400, "文本消息不能为空");
        }
        if (MessageTypeConstant.IMAGE.equals(messageType) && (sendDTO.getImageUrl() == null || sendDTO.getImageUrl().trim().isEmpty())) {
            log.warn("图片地址不能为空");
            throw new BaseException(400, "图片地址不能为空");
        }

        // 3 核心事务（仅DB）
        PrivateMessageRealtimeVO vo = getSelf().saveMessageAndConversation(sendDTO, senderId, receiverId);
        log.info("发送私信消息，VO: {}", vo);
        // 4. 推送消息
        wsHandler.sendToUser(receiverId, vo);

        return vo;
    }


    /*
     * 获取私信消息历史记录
     * @param peerId 对方用户ID
     * @param lastMessageId 上一条消息ID
     * @param pageSize 分页大小
     * @return 私信消息VO列表
     */
    @Override
    public List<PrivateMessageVO> getChatHistoryByCursor(Long peerId, Long lastMessageId, Integer pageSize) {
        Long currentUserId = BaseContext.getCurrentId();

        SysUser peer = userDao.selectById(peerId);
        if (peer == null) {
            log.warn("对方用户不存在，ID: {}", peerId);
            throw new AbsentException("对方用户不存在");
        }
        BizPrivateConversation conversation = conversationDao.selectByUserIdAndPeerId(currentUserId, peerId);
        // 获取对话清除时间
        LocalDateTime clearBefore = conversation != null ? conversation.getClearBeforeTime() : null;
        log.info("获取私信消息历史记录，用户ID: {}, 对方用户ID: {}, 上一条消息ID: {}, 分页大小: {}", currentUserId, peerId, lastMessageId, pageSize);
        List<PrivateMessageVO> list = messageDao.selectChatHistoryByCursor(currentUserId, peerId, clearBefore, lastMessageId, pageSize);
        log.info("获取私信消息历史记录，用户ID: {}, 对方用户ID: {}, 上一条消息ID: {}, 分页大小: {}, 消息数量: {}", currentUserId, peerId, lastMessageId, pageSize, list.size());
        Collections.reverse(list);
        return list;
    }

    /**
     * 标记对话为已读
     *
     * @param peerId 对方用户ID
     */
    @Override
    public void markConversationAsRead(Long peerId) {
        Long currentUserId = BaseContext.getCurrentId();
        messageDao.updateStatusToRead(peerId, currentUserId);
    }

    /**
     * 清空对话
     *
     * @param peerId 对方用户ID
     */
    @Override
    public void clearConversation(Long peerId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("清空对话，用户ID: {}, 对方用户ID: {}", currentUserId, peerId);
        BizPrivateConversation conversation = conversationDao.selectByUserIdAndPeerId(currentUserId, peerId);
        if (conversation == null) {
            log.info("对话不存在，创建新对话");
            conversation = new BizPrivateConversation();
            conversation.setUserId(currentUserId);
            conversation.setPeerId(peerId);
            // 设置清除时间当前时间
            conversation.setClearBeforeTime(LocalDateTime.now());
            conversationDao.insert(conversation);
        } else {
            log.info("对话存在，更新清除时间");
            conversation.setClearBeforeTime(LocalDateTime.now());
            conversationDao.updateById(conversation);
        }
    }

    /**
     * 获取私信对话列表
     *
     * @return
     */
    @Override
    public List<ConversationVO> getConversationList() {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("获取私信对话列表，用户ID: {}", currentUserId);
        List<ConversationVO> list = messageDao.selectConversationList(currentUserId);
        log.info("获取私信对话列表成功，用户ID: {}, 总对话数: {}", currentUserId, list.size());
        return list;
    }

    /**
     * 删除消息
     *
     * @param messageId 消息ID
     *                  <p>
     *                  1. 验证消息是否存在
     *                  2. 验证用户权限
     *                  3. 更新消息状态
     *                  4. 更新数据库
     */
    @Override
    public void deleteMessage(Long messageId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("删除私信消息，用户ID: {}, 消息ID: {}", currentUserId, messageId);
        //1. 验证消息
        BizPrivateMessage message = getById(messageId);
        if (message == null) {
            log.warn("消息不存在，ID: {}", messageId);
            throw new AbsentException("消息不存在");
        }
        //2. 验证用户权限
        if (!currentUserId.equals(message.getSenderId()) && !currentUserId.equals(message.getReceiverId())) {
            log.warn("用户{}无权删除消息{}", currentUserId, messageId);
            throw new BaseException(403, "无权删除该消息");
        }
        //3. 更新消息状态
        if (currentUserId.equals(message.getSenderId())) {
            message.setSenderDeleted(DeletedConstant.DELETED);
        } else {
            message.setReceiverDeleted(DeletedConstant.DELETED);
        }
        //4. 更新数据库
        updateById(message);
    }

    /**
     * 删除对话
     *
     * @param peerId 对方用户ID
     *               1. 发送方视角
     *               2. 接收方视角
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long peerId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("删除私信对话，用户ID: {}, 对方用户ID: {}", currentUserId, peerId);
        // 1发送方视角
        lambdaUpdate()
                .eq(BizPrivateMessage::getSenderId, currentUserId)
                .eq(BizPrivateMessage::getReceiverId, peerId)
                .set(BizPrivateMessage::getSenderDeleted, DeletedConstant.DELETED)
                .update();

        // 2接收方视角
        lambdaUpdate()
                .eq(BizPrivateMessage::getSenderId, peerId)
                .eq(BizPrivateMessage::getReceiverId, currentUserId)
                .set(BizPrivateMessage::getReceiverDeleted, DeletedConstant.DELETED)
                .update();
    }

    /**
     * 获取未读消息数量
     *
     * @return
     */
    @Override
    public Long getUnreadCount() {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("获取未读消息数量，用户ID: {}", currentUserId);
        Long count = lambdaQuery()
                .eq(BizPrivateMessage::getReceiverId, currentUserId)
                .eq(BizPrivateMessage::getStatus, ReadStatusConstant.UNREAD) // 0 = UNREAD
                .eq(BizPrivateMessage::getReceiverDeleted, DeletedConstant.DELETED)
                .count();
        log.info("获取未读消息数量成功，用户ID: {}, 未读消息数: {}", currentUserId, count);
        return count;
    }


    /**
     * 事务方法：保存消息 + 会话（原子性）
     *
     * @param sendDTO    私信消息发送DTO
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     *                   <p>
     *                   1. 保存消息
     *                   2. 更新发送者会话
     *                   3. 接收者会话（不存在则创建）
     *                   4. 返回VO
     */
    @Transactional(rollbackFor = Exception.class)
    public PrivateMessageRealtimeVO saveMessageAndConversation(PrivateMessageSendDTO sendDTO,
                                                               Long senderId,
                                                               Long receiverId) {
        // 1.保存消息
        BizPrivateMessage message = new BizPrivateMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(sendDTO.getContent());
        message.setMessageType(sendDTO.getMessageType());
        message.setImageUrl(sendDTO.getImageUrl());
        message.setStatus(ReadStatusConstant.UNREAD);
        message.setClientMsgId(sendDTO.getClientMsgId());
        message.setSenderDeleted(0);
        message.setReceiverDeleted(0);
        save(message);

        // 2. 更新发送者会话
        BizPrivateConversation senderConversation = conversationDao.selectByUserIdAndPeerId(senderId, receiverId);
        if (senderConversation == null) {
            senderConversation = new BizPrivateConversation();
            senderConversation.setUserId(senderId);
            senderConversation.setPeerId(receiverId);
            senderConversation.setLastReadMessageId(message.getId());
            conversationDao.insert(senderConversation);
        } else {
            senderConversation.setLastReadMessageId(message.getId());
            conversationDao.updateById(senderConversation);
        }

        // 3. 接收者会话（不存在则创建）
        BizPrivateConversation receiverConversation = conversationDao.selectByUserIdAndPeerId(receiverId, senderId);
        if (receiverConversation == null) {
            receiverConversation = new BizPrivateConversation();
            receiverConversation.setUserId(receiverId);
            receiverConversation.setPeerId(senderId);
            conversationDao.insert(receiverConversation);
        }

        // 4.返回VO
        PrivateMessageRealtimeVO vo = new PrivateMessageRealtimeVO();
        vo.setId(message.getId());
        vo.setSenderId(senderId);
        vo.setReceiverId(receiverId);
        vo.setContent(message.getContent());
        vo.setMessageType(message.getMessageType());
        vo.setImageUrl(message.getImageUrl());
        vo.setStatus(message.getStatus());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

}
