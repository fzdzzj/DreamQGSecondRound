package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qg.pojo.entity.AiChatHistory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.Instant;

/**
 *
 */
@Mapper
public interface ChatHistoryDao extends BaseMapper<AiChatHistory> {
    /**
     * 获取对话历史的第一条消息时间
     *
     * @param type   对话类型
     * @param chatId 会话 ID
     * @return 第一条消息时间
     */
    @Select("SELECT MIN(create_time) FROM ai_chat_history WHERE type=#{type} AND chat_id=#{chatId}")
    Instant getFirstMessageTime(@Param("type") String type, @Param("chatId") String chatId);

    /**
     * 删除对话历史记录
     *
     * @param type   对话类型
     * @param chatId 会话 ID
     */
    @Delete("DELETE FROM ai_chat_history WHERE type=#{type} AND chat_id=#{chatId}")
    void deleteChatHistory(@Param("type") String type, @Param("chatId") String chatId);
}
