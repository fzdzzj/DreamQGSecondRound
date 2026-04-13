package com.qg.server.ai.memory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TemporaryChatMemory implements ChatMemory {

    private final Map<String, List<Message>> memory = new ConcurrentHashMap<>();
    private final Map<String, Instant> creationTime = new ConcurrentHashMap<>();

    @Override
    public List<Message> get(String chatId, int maxMessages) {
        List<Message> list = memory.getOrDefault(chatId, List.of());
        if (list.size() > maxMessages) {
            return list.subList(list.size() - maxMessages, list.size());
        }
        return new ArrayList<>(list);
    }

    @Override
    public void clear(String conversationId) {
        memory.remove(conversationId);
        creationTime.remove(conversationId);
    }

    @Override
    public void add(String chatId, Message message) {
        memory.computeIfAbsent(chatId, k -> new ArrayList<>()).add(message);
        creationTime.putIfAbsent(chatId, Instant.now());
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) return;
        memory.computeIfAbsent(conversationId, k -> new ArrayList<>()).addAll(messages);
        creationTime.putIfAbsent(conversationId, Instant.now());
    }

    public void cleanExpired(long ttlSeconds) {
        Instant now = Instant.now();
        creationTime.forEach((chatId, created) -> {
            if (now.getEpochSecond() - created.getEpochSecond() > ttlSeconds) {
                memory.remove(chatId);
                creationTime.remove(chatId);
            }
        });
    }
}
