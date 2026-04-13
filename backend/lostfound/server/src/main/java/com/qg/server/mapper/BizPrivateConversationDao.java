package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.BizPrivateConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BizPrivateConversationDao extends BaseMapper<BizPrivateConversation> {
    BizPrivateConversation selectByUserIdAndPeerId(@Param("userId") Long userId, @Param("peerId") Long peerId);
}
