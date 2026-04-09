package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qg.pojo.entity.BizPrivateMessage;
import com.qg.pojo.vo.ConversationVO;
import com.qg.pojo.vo.PrivateMessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BizPrivateMessageDao extends BaseMapper<BizPrivateMessage> {

    /**
     * 查询会话列表
     *
     * @param currentUserId 当前用户ID
     * @return 会话列表
     */
    List<ConversationVO> selectConversationList(@Param("currentUserId") Long currentUserId);

    /**
     * 查询聊天记录
     *
     * @param page 分页对象
     * @param currentUserId 当前用户ID
     * @param peerId 对方用户ID
     * @return 聊天记录
     */
    Page<PrivateMessageVO> selectChatHistory(Page<PrivateMessageVO> page,
                                             @Param("currentUserId") Long currentUserId,
                                             @Param("peerId") Long peerId);
}
