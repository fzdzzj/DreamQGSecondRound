package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.AiChatHistory;
import org.apache.ibatis.annotations.*;

import java.time.Instant;

@Mapper
public interface ChatHistoryDao extends BaseMapper<AiChatHistory> {

    @Select("SELECT MIN(create_time) FROM ai_chat_history WHERE type=#{type} AND chat_id=#{chatId}")
    Instant getFirstMessageTime(@Param("type") String type, @Param("chatId") String chatId);

    @Delete("DELETE FROM ai_chat_history WHERE type=#{type} AND chat_id=#{chatId}")
    void deleteChatHistory(@Param("type") String type, @Param("chatId") String chatId);
}
