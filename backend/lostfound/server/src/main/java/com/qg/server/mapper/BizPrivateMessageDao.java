package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizPrivateMessage;
import com.qg.pojo.vo.ConversationVO;
import com.qg.pojo.vo.PrivateMessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 私聊消息数据访问层
 */
@Mapper
public interface BizPrivateMessageDao extends BaseMapper<BizPrivateMessage> {
    /**
     * 查询私聊消息记录，根据游标分页查询
     *
     * @param currentUserId   当前用户ID
     * @param peerId          对话用户ID
     * @param clearBeforeTime
     * @param lastMessageId
     * @param pageSize
     * @return
     */
    List<PrivateMessageVO> selectChatHistoryByCursor(
            @Param("currentUserId") Long currentUserId,
            @Param("peerId") Long peerId,
            @Param("clearBeforeTime") LocalDateTime clearBeforeTime,
            @Param("lastMessageId") Long lastMessageId,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 更新私聊消息状态为已读
     *
     * @param peerId        对话用户ID
     * @param currentUserId 当前用户ID
     */
    void updateStatusToRead(@Param("peerId") Long peerId, @Param("currentUserId") Long currentUserId);

    /**
     * 查询私聊会话列表
     *
     * @param currentUserId 当前用户ID
     * @return 私聊会话列表VO
     */
    List<ConversationVO> selectConversationList(Long currentUserId);
}
