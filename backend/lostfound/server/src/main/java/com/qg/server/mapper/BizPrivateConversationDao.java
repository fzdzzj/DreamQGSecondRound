package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizPrivateConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 私聊数据访问层
 */
@Mapper
public interface BizPrivateConversationDao extends BaseMapper<BizPrivateConversation> {
    /**
     * 根据用户ID和会话对象ID查询私聊记录
     * @param userId 用户ID
     * @param peerId 对话用户ID
     * @return 私聊记录对象
     */
    BizPrivateConversation selectByUserIdAndPeerId(@Param("userId") Long userId, @Param("peerId") Long peerId);
}
