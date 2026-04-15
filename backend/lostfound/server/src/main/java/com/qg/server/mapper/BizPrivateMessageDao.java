package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizPrivateMessage;
import com.qg.pojo.vo.ConversationVO;
import com.qg.pojo.vo.PrivateMessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BizPrivateMessageDao extends BaseMapper<BizPrivateMessage> {

    List<PrivateMessageVO> selectHistoryByCursor(@Param("currentUserId") Long currentUserId,
                                                 @Param("peerId") Long peerId,
                                                 @Param("lastMessageId") Long lastMessageId,
                                                 @Param("pageSize") Integer pageSize);

    List<ConversationVO> selectConversationList(@Param("currentUserId") Long currentUserId);

    void markConversationDeletedForSender(@Param("currentUserId") Long currentUserId,
                                          @Param("peerId") Long peerId);

    void markConversationDeletedForReceiver(@Param("currentUserId") Long currentUserId,
                                            @Param("peerId") Long peerId);
}
