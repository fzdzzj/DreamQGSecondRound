package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizPrivateMessage;
import com.qg.pojo.vo.ConversationVO;
import com.qg.pojo.vo.PrivateMessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BizPrivateMessageDao extends BaseMapper<BizPrivateMessage> {

    List<PrivateMessageVO> selectChatHistoryByCursor(
            @Param("currentUserId") Long currentUserId,
            @Param("peerId") Long peerId,
            @Param("clearBeforeTime") LocalDateTime clearBeforeTime,
            @Param("lastMessageId") Long lastMessageId,
            @Param("pageSize") Integer pageSize
    );

    void updateStatusToRead(@Param("peerId") Long peerId, @Param("currentUserId") Long currentUserId);

    List<ConversationVO> selectConversationList(Long currentUserId);
}
