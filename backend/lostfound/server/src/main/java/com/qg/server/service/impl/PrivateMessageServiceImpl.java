package com.qg.server.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.MessageConstant;
import com.qg.common.constant.ReadStatusConstant;
import com.qg.common.context.BaseContext;
import com.qg.common.result.PageResult;
import com.qg.common.exception.AbsentException;
import com.qg.common.exception.BaseException;
import com.qg.pojo.dto.PrivateMessageSendDTO;
import com.qg.pojo.entity.BizPrivateMessage;
import com.qg.pojo.entity.SysUser;
import com.qg.pojo.vo.ConversationVO;
import com.qg.pojo.vo.PrivateMessageVO;
import com.qg.server.mapper.BizPrivateMessageDao;
import com.qg.server.mapper.UserDao;
import com.qg.server.service.PrivateMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 私聊消息业务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PrivateMessageServiceImpl
        extends ServiceImpl<BizPrivateMessageDao, BizPrivateMessage>
        implements PrivateMessageService {

    private final BizPrivateMessageDao bizPrivateMessageDao;
    private final UserDao userDao;

    /**
     * 发送私聊消息
     *
     * 说明：
     * 1. 发送者从上下文中获取，前端不传 senderId
     * 2. 不允许给自己发消息
     * 3. 接收者必须存在
     * 4. 新消息默认未读、双方都未删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(PrivateMessageSendDTO sendDTO) {
        Long senderId = BaseContext.getCurrentId();
        Long receiverId = sendDTO.getReceiverId();

        log.info("发送私聊消息开始，senderId={}, receiverId={}", senderId, receiverId);

        if (senderId.equals(receiverId)) {
            log.warn("发送私聊消息失败，不能给自己发消息，senderId={}", senderId);
            throw new BaseException(400, MessageConstant.PRIVATE_MESSAGE_SEND_TO_SELF_NOT_ALLOWED);
        }

        SysUser receiver = userDao.selectById(receiverId);
        if (receiver == null) {
            log.warn("发送私聊消息失败，接收用户不存在，receiverId={}", receiverId);
            throw new AbsentException(MessageConstant.PRIVATE_MESSAGE_RECEIVER_NOT_FOUND);
        }

        BizPrivateMessage message = new BizPrivateMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(sendDTO.getContent().trim());
        message.setStatus(ReadStatusConstant.UNREAD);
        message.setSenderDeleted(0);
        message.setReceiverDeleted(0);

        save(message);

        log.info("发送私聊消息成功，messageId={}, senderId={}, receiverId={}",
                message.getId(), senderId, receiverId);
    }

    /**
     * 获取当前用户会话列表
     */
    @Override
    public List<ConversationVO> getConversationList() {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("查询会话列表，userId={}", currentUserId);

        List<ConversationVO> list = bizPrivateMessageDao.selectConversationList(currentUserId);

        log.info("查询会话列表成功，userId={}, size={}", currentUserId, list.size());
        return list;
    }

    /**
     * 获取聊天记录
     *
     * 说明：
     * 1. 仅查询当前用户可见的消息
     * 2. 发送方删除后，对发送方不可见
     * 3. 接收方删除后，对接收方不可见
     */
    @Override
    public PageResult<PrivateMessageVO> getChatHistory(Long peerId, Integer pageNum, Integer pageSize) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("查询聊天记录，userId={}, peerId={}, pageNum={}, pageSize={}",
                currentUserId, peerId, pageNum, pageSize);

        SysUser peer = userDao.selectById(peerId);
        if (peer == null) {
            log.warn("查询聊天记录失败，对方用户不存在，peerId={}", peerId);
            throw new AbsentException(MessageConstant.PRIVATE_MESSAGE_RECEIVER_NOT_FOUND);
        }

        Page<PrivateMessageVO> page = new Page<>(pageNum, pageSize);
        Page<PrivateMessageVO> resultPage = bizPrivateMessageDao.selectChatHistory(page, currentUserId, peerId);

        log.info("查询聊天记录成功，userId={}, peerId={}, total={}",
                currentUserId, peerId, resultPage.getTotal());

        return new PageResult<>(
                resultPage.getRecords(),
                resultPage.getTotal(),
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize()
        );
    }

    /**
     * 将整个会话标记为已读
     *
     * 说明：
     * 仅把“对方发给我、且我还没删掉”的消息置为已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markConversationAsRead(Long peerId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("标记会话已读开始，userId={}, peerId={}", currentUserId, peerId);

        lambdaUpdate()
                .eq(BizPrivateMessage::getSenderId, peerId)
                .eq(BizPrivateMessage::getReceiverId, currentUserId)
                .eq(BizPrivateMessage::getStatus, ReadStatusConstant.UNREAD)
                .eq(BizPrivateMessage::getReceiverDeleted, 0)
                .set(BizPrivateMessage::getStatus, ReadStatusConstant.READ)
                .update();

        log.info("标记会话已读成功，userId={}, peerId={}", currentUserId, peerId);
    }

    /**
     * 删除单条消息
     *
     * 说明：
     * 1. 仅当前消息参与者可删
     * 2. 删除是“当前用户视角删除”
     * 3. 不影响对方查看
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("删除单条消息开始，userId={}, messageId={}", currentUserId, messageId);

        BizPrivateMessage message = getById(messageId);
        if (message == null) {
            log.warn("删除单条消息失败，消息不存在，messageId={}", messageId);
            throw new AbsentException(MessageConstant.PRIVATE_MESSAGE_NOT_FOUND);
        }

        if (!currentUserId.equals(message.getSenderId()) && !currentUserId.equals(message.getReceiverId())) {
            log.warn("删除单条消息失败，无权操作，userId={}, messageId={}", currentUserId, messageId);
            throw new BaseException(403, MessageConstant.PRIVATE_MESSAGE_DELETE_NOT_ALLOWED);
        }

        BizPrivateMessage update = new BizPrivateMessage();
        update.setId(messageId);

        if (currentUserId.equals(message.getSenderId())) {
            update.setSenderDeleted(1);
        } else {
            update.setReceiverDeleted(1);
        }

        updateById(update);

        log.info("删除单条消息成功，userId={}, messageId={}", currentUserId, messageId);
    }

    /**
     * 删除会话
     *
     * 说明：
     * 1. 仅当前用户视角下删除
     * 2. 本质是批量把当前用户与对方的消息标记为“当前用户已删除”
     * 3. 不影响对方会话
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long peerId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("删除会话开始，userId={}, peerId={}", currentUserId, peerId);

        lambdaUpdate()
                .eq(BizPrivateMessage::getSenderId, currentUserId)
                .eq(BizPrivateMessage::getReceiverId, peerId)
                .set(BizPrivateMessage::getSenderDeleted, 1)
                .update();

        lambdaUpdate()
                .eq(BizPrivateMessage::getSenderId, peerId)
                .eq(BizPrivateMessage::getReceiverId, currentUserId)
                .set(BizPrivateMessage::getReceiverDeleted, 1)
                .update();

        log.info("删除会话成功，userId={}, peerId={}", currentUserId, peerId);
    }

    /**
     * 获取当前用户未读消息总数
     */
    @Override
    public Long getUnreadCount() {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("查询未读消息总数，userId={}", currentUserId);

        Long count = lambdaQuery()
                .eq(BizPrivateMessage::getReceiverId, currentUserId)
                .eq(BizPrivateMessage::getStatus, ReadStatusConstant.UNREAD)
                .eq(BizPrivateMessage::getReceiverDeleted, 0)
                .count();

        log.info("查询未读消息总数成功，userId={}, count={}", currentUserId, count);
        return count;
    }
}
