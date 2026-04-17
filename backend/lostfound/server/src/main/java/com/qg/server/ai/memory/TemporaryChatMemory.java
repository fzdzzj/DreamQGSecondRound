package com.qg.server.ai.memory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 临时存储
 */
@Component
public class TemporaryChatMemory implements ChatMemory {
    /**
     * 临时存储
     */
    private final Map<String, List<Message>> memory = new ConcurrentHashMap<>();
    /**
     * 创建时间
     */
    private final Map<String, Instant> creationTime = new ConcurrentHashMap<>();


    /**
     * 清除临时存储
     */
    @Override
    public void clear(String conversationId) {
        memory.remove(conversationId);
        creationTime.remove(conversationId);
    }

    /**
     * 添加消息到临时存储
     */
    @Override
    public void add(String chatId, Message message) {
        // 如果临时存储不存在,则创建一个新的列表,添加消息
        // 如果临时存储存在,则添加消息
        memory.computeIfAbsent(chatId, k -> new ArrayList<>()).add(message);
        // 更新创建时间
        creationTime.putIfAbsent(chatId, Instant.now());
    }

    /**
     * 批量添加消息到临时存储
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        // 如果消息为空,则直接返回
        if (messages == null || messages.isEmpty()) return;
        // 批量添加消息
        memory.computeIfAbsent(conversationId, k -> new ArrayList<>()).addAll(messages);
        // 更新创建时间
        creationTime.putIfAbsent(conversationId, Instant.now());
    }

    /**
     * 获取临时存储
     */
    @Override
    public List<Message> get(String conversationId) {
        return memory.getOrDefault(conversationId, List.of());
    }

    /**
     * 清除过期的临时存储
     */
    public void cleanExpired(long ttlSeconds) {
        Instant now = Instant.now();
        // 遍历创建时间,如果创建时间大于过期时间,则清除临时存储和创建时间
        creationTime.forEach((chatId, created) -> {
            if (now.getEpochSecond() - created.getEpochSecond() > ttlSeconds) {
                memory.remove(chatId);
                creationTime.remove(chatId);
            }
        });
    }
}
