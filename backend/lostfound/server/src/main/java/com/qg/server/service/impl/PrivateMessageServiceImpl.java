package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.DeletedConstant;
import com.qg.common.constant.ReadStatusConstant;
import com.qg.common.context.BaseContext;
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
import com.qg.server.websocket.model.WsMessageType;
import com.qg.server.websocket.model.WsPrivateMessagePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrivateMessageServiceImpl extends ServiceImpl<BizPrivateMessageDao, BizPrivateMessage> implements PrivateMessageService {

    private final BizPrivateMessageDao privateMessageDao;
    private final BizPrivateConversationDao privateConversationDao;
    private final UserDao sysUserDao;
    private final PrivateChatWebSocketHandler privateChatWebSocketHandler;
    private final ApplicationContext applicationContext;

    private PrivateMessageServiceImpl getSelf() {
        return applicationContext.getBean(PrivateMessageServiceImpl.class);
    }

    /**
     * 发送私聊消息
     *
     * @param dto 消息发送DTO
     *            1. 检查当前用户是否登录
     *            2. 检查当前用户是否是接收用户
     *            3. 检查接收用户是否存在
     *            4. 检查消息类型是否支持
     *            5. 检查消息内容是否为空
     *            6. 检查图片地址是否为空
     *            7. 创建并插入私聊消息
     *            8. 给接收方推送新消息
     *            9. 给接收方推送未读数变化
     */
    @Override
    public void sendMessage(PrivateMessageSendDTO dto) {
        Long senderId = BaseContext.getCurrentId();
        // 1. 检查当前用户是否登录
        if (senderId == null) {
            log.warn("当前用户未登录");
            throw new BaseException(401, "当前用户未登录");
        }
        // 2. 检查当前用户是否是接收用户
        if (senderId.equals(dto.getReceiverId())) {
            log.warn("不能给自己发送消息");
            throw new BaseException(400, "不能给自己发送消息");
        }
        // 3. 检查接收用户是否存在
        SysUser receiver = sysUserDao.selectById(dto.getReceiverId());
        if (receiver == null) {
            log.warn("接收用户不存在");
            throw new BaseException(400, "接收用户不存在");
        }
        // 1 = 文本，2 = 图片
        if (dto.getMessageType().equals("1")) {
            // 文本消息
            if (dto.getContent() == null || dto.getContent().isBlank()) {
                log.warn("文本消息内容不能为空");
                throw new BaseException(400, "文本消息内容不能为空");
            }
        } else if (dto.getMessageType().equals("2")) {
            // 检查图片地址是否为空
            if (dto.getImageUrl() == null || dto.getImageUrl().isBlank()) {
                log.warn("图片消息地址不能为空");
                throw new BaseException(400, "图片消息地址不能为空");
            }
        } else {
            // 不支持的消息类型
            log.warn("不支持的消息类型");
            throw new BaseException(400, "不支持的消息类型");
        }
        // 创建并插入私聊消息
        log.info("创建并插入私聊消息");
        BizPrivateMessage entity = getSelf().createAndInsertMessage(senderId, dto);
        PrivateMessageRealtimeVO realtimeVO = new PrivateMessageRealtimeVO();
        BeanUtils.copyProperties(entity, realtimeVO);
        log.error("发送私聊消息给userId={}, type={}", dto.getReceiverId(), WsMessageType.PRIVATE_MESSAGE);
        privateChatWebSocketHandler.sendToUser(
                dto.getReceiverId(),
                WsPrivateMessagePayload.from(realtimeVO),
                WsMessageType.PRIVATE_MESSAGE
        );

        // 7. 给接收方推送未读数变化
        long receiverTotalUnread = getUnreadCountByUserId(dto.getReceiverId());
        long receiverConversationUnread = getConversationUnreadCount(dto.getReceiverId(), senderId);
        privateChatWebSocketHandler.sendToUser(
                dto.getReceiverId(),
                new UnreadChangedPayload(receiverTotalUnread, receiverConversationUnread, senderId),
                WsMessageType.UNREAD_CHANGED
        );

        // 8. 给发送方也推送一次当前会话未读变化（通常为 0），便于前端统一刷新会话列表
        long senderTotalUnread = getUnreadCountByUserId(senderId);
        long senderConversationUnread = getConversationUnreadCount(senderId, dto.getReceiverId());
        privateChatWebSocketHandler.sendToUser(
                senderId,
                new UnreadChangedPayload(senderTotalUnread, senderConversationUnread, dto.getReceiverId()),
                WsMessageType.UNREAD_CHANGED
        );
    }

    @Override
    public List<PrivateMessageVO> getHistoryByCursor(Long peerId, Long lastMessageId, Integer pageSize) {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new BaseException("当前用户未登录");
        }
        if (peerId == null) {
            throw new BaseException("会话对象不能为空");
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 20;
        }
        return privateMessageDao.selectHistoryByCursor(currentUserId, peerId, lastMessageId, pageSize);
    }

    /**
     * 标记对话已读
     * 1. 检查当前用户是否登录
     * 2. 检查当前用户是否是会话对象
     * 3. 给发送方推送未读数变化
     */
    @Override
    public void markConversationRead(Long peerId) {
        Long currentUserId = BaseContext.getCurrentId();
        log.info("标记对话已读");
        // 1. 检查当前用户是否登录
        if (currentUserId == null) {
            log.warn("当前用户未登录");
            throw new BaseException(401, "当前用户未登录");
        }
        // 2. 检查当前用户是否是会话对象
        if (peerId == null) {
            log.warn("会话对象不能为空");
            throw new BaseException(400, "会话对象不能为空");
        }
        // 3. 标记对话已读
        privateMessageDao.update(
                null,
                new LambdaUpdateWrapper<BizPrivateMessage>()
                        .eq(BizPrivateMessage::getSenderId, peerId)
                        .eq(BizPrivateMessage::getReceiverId, currentUserId)
                        .eq(BizPrivateMessage::getStatus, ReadStatusConstant.UNREAD)
                        .set(BizPrivateMessage::getStatus, ReadStatusConstant.READ)
        );
        // 4. 给发送方推送未读数变化
        long totalUnread = getUnreadCountByUserId(currentUserId);
        privateChatWebSocketHandler.sendToUser(
                currentUserId,
                new UnreadChangedPayload(totalUnread, 0L, peerId),
                WsMessageType.UNREAD_CHANGED
        );
    }

    /**
     * 获取未读消息列表
     */
    @Override
    public List<ConversationVO> getConversations() {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new BaseException("当前用户未登录");
        }
        return privateMessageDao.selectConversationList(currentUserId);
    }

    /**
     * 删除消息
     * 1. 检查当前用户是否登录
     * 2. 检查消息ID是否为空
     * 3. 检查消息是否存在
     * 4. 检查当前用户是否是发送方或接收方
     * 5. 删除消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId) {
        Long currentUserId = BaseContext.getCurrentId();
        // 1. 检查当前用户是否登录
        if (currentUserId == null) {
            log.warn("当前用户未登录");
            throw new BaseException(401, "当前用户未登录");
        }
        // 2. 检查消息ID是否为空
        if (messageId == null) {
            log.warn("消息ID不能为空");
            throw new BaseException(400, "消息ID不能为空");
        }
        // 3. 检查消息是否存在
        BizPrivateMessage message = privateMessageDao.selectById(messageId);
        if (message == null) {
            log.warn("消息不存在");
            throw new BaseException(400, "消息不存在");
        }
        // 4. 检查当前用户是否是发送方或接收方
        if (currentUserId.equals(message.getSenderId())) {
            privateMessageDao.update(
                    null,
                    new LambdaUpdateWrapper<BizPrivateMessage>()
                            .eq(BizPrivateMessage::getId, messageId)
                            .set(BizPrivateMessage::getSenderDeleted, 1)
            );
            return;
        }

        if (currentUserId.equals(message.getReceiverId())) {
            privateMessageDao.update(
                    null,
                    new LambdaUpdateWrapper<BizPrivateMessage>()
                            .eq(BizPrivateMessage::getId, messageId)
                            .set(BizPrivateMessage::getReceiverDeleted, 1)
            );
            return;
        }
        log.warn("当前用户不是发送方或接收方");
        throw new BaseException(403, "无权删除该消息");
    }

    /**
     * 删除会话
     * 1. 检查当前用户是否登录
     * 2. 检查会话对象是否为空
     * 3. 删除会话
     * 4. 给发送方推送未读数变化
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long peerId) {
        log.info("删除会话", peerId);
        Long currentUserId = BaseContext.getCurrentId();
        // 1. 检查当前用户是否登录
        if (currentUserId == null) {
            log.warn("当前用户未登录");
            throw new BaseException(401, "当前用户未登录");
        }
        // 2. 检查会话对象是否为空
        if (peerId == null) {
            log.warn("会话对象不能为空");
            throw new BaseException(400, "会话对象不能为空");
        }
        // 3. 删除会话
        privateMessageDao.markConversationDeletedForSender(currentUserId, peerId);
        privateMessageDao.markConversationDeletedForReceiver(currentUserId, peerId);
        privateConversationDao.deleteConversation(currentUserId, peerId);
        // 4. 给发送方推送未读数变化
        long totalUnread = getUnreadCountByUserId(currentUserId);
        privateChatWebSocketHandler.sendToUser(
                currentUserId,
                new UnreadChangedPayload(totalUnread, 0L, peerId),
                WsMessageType.UNREAD_CHANGED
        );
    }

    /**
     * 获取未读数
     */
    @Override
    public Long getUnreadCount() {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new BaseException("当前用户未登录");
        }
        return getUnreadCountByUserId(currentUserId);
    }

    /**
     * 清空会话
     * 1. 检查当前用户是否登录
     * 2. 检查会话对象是否为空
     * 3. 删除会话
     * 4. 给发送方推送未读数变化
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearConversation(Long peerId) {
        Long currentUserId = BaseContext.getCurrentId();
        // 1. 检查当前用户是否登录
        if (currentUserId == null) {
            log.warn("当前用户未登录");
            throw new BaseException(401, "当前用户未登录");
        }
        if (peerId == null) {
            log.warn("会话对象不能为空");
            throw new BaseException(400, "会话对象不能为空");
        }
        // 2. 检查会话是否存在
        BizPrivateConversation conversation = privateConversationDao.selectOne(
                new LambdaQueryWrapper<BizPrivateConversation>()
                        .eq(BizPrivateConversation::getUserId, currentUserId)
                        .eq(BizPrivateConversation::getPeerId, peerId)
                        .last("limit 1")
        );
        // 3. 清空会话
        if (conversation == null) {
            log.warn("会话不存在");
            conversation = new BizPrivateConversation();
            conversation.setUserId(currentUserId);
            conversation.setPeerId(peerId);
            conversation.setClearBeforeTime(LocalDateTime.now());
            privateConversationDao.insert(conversation);
        } else {
            log.info("清空会话{}", conversation);
            conversation.setClearBeforeTime(LocalDateTime.now());
            privateConversationDao.updateById(conversation);
        }
        // 4. 给发送方推送未读数变化
        long totalUnread = getUnreadCountByUserId(currentUserId);
        privateChatWebSocketHandler.sendToUser(
                currentUserId,
                new UnreadChangedPayload(totalUnread, 0L, peerId),
                WsMessageType.UNREAD_CHANGED
        );
    }

    /**
     * 创建并插入消息
     *
     * @param senderId
     * @param dto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    protected BizPrivateMessage createAndInsertMessage(Long senderId, PrivateMessageSendDTO dto) {
        log.info("创建并插入消息{}", dto);
        BizPrivateMessage entity = new BizPrivateMessage();
        entity.setSenderId(senderId);
        entity.setReceiverId(dto.getReceiverId());
        entity.setContent(dto.getContent());
        entity.setMessageType(dto.getMessageType());
        entity.setImageUrl(dto.getImageUrl());
        entity.setClientMsgId(dto.getClientMsgId());
        entity.setStatus(0);
        entity.setSenderDeleted(0);
        entity.setReceiverDeleted(0);
        privateMessageDao.insert(entity);
        log.info("创建并插入消息{}", entity);
        upsertConversation(senderId, dto.getReceiverId());
        upsertConversation(dto.getReceiverId(), senderId);

        return entity;
    }

    /**
     * 创建并插入会话
     */
    private void upsertConversation(Long userId, Long peerId) {
        BizPrivateConversation conversation = privateConversationDao.selectOne(
                new LambdaQueryWrapper<BizPrivateConversation>()
                        .eq(BizPrivateConversation::getUserId, userId)
                        .eq(BizPrivateConversation::getPeerId, peerId)
                        .last("limit 1")
        );

        if (conversation == null) {
            conversation = new BizPrivateConversation();
            conversation.setUserId(userId);
            conversation.setPeerId(peerId);
            privateConversationDao.insert(conversation);
        } else {
            privateConversationDao.updateById(conversation);
        }
    }

    /**
     * 获取未读数
     *
     * @param userId 用户ID
     */
    private long getUnreadCountByUserId(Long userId) {
        return privateMessageDao.selectCount(
                new LambdaQueryWrapper<BizPrivateMessage>()
                        .eq(BizPrivateMessage::getReceiverId, userId)
                        .eq(BizPrivateMessage::getStatus, 0)
                        .eq(BizPrivateMessage::getReceiverDeleted, DeletedConstant.NOT_DELETED)
        );
    }

    /**
     * 获取会话未读数
     *
     * @param currentUserId 当前用户ID
     * @param peerId        对话用户ID
     */
    private long getConversationUnreadCount(Long currentUserId, Long peerId) {
        return privateMessageDao.selectCount(
                new LambdaQueryWrapper<BizPrivateMessage>()
                        .eq(BizPrivateMessage::getSenderId, peerId)
                        .eq(BizPrivateMessage::getReceiverId, currentUserId)
                        .eq(BizPrivateMessage::getStatus, 0)
                        .eq(BizPrivateMessage::getReceiverDeleted, 0)
        );
    }

    /**
     * 未读数变化载荷
     *
     * @param totalUnreadCount
     * @param conversationUnreadCount
     * @param peerId
     */
    public record UnreadChangedPayload(Long totalUnreadCount, Long conversationUnreadCount, Long peerId) {

    }
}
