package com.qg.server.controller.ai;

import com.qg.pojo.vo.MessageVO;
import com.qg.server.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/history")
public class ChatHistoryController {
    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatMemory chatMemory;
    @GetMapping("/chatIds")
    public List<String> getChatIds(@RequestParam("type") String type, @RequestParam("userId") Long userId) {
        return chatHistoryRepository.getChatIds(type,userId);
    }

    @GetMapping("/chatHistory")
    public List<MessageVO> getChatHistory(@RequestParam("type") String type, @RequestParam("chatId") String chatId, @RequestParam("userId") Long userId) {
        List<Message> messages = chatMemory.get(chatId, Integer.MAX_VALUE);
        if (messages == null) return List.of();
        return messages.stream().map(MessageVO::new).toList();
    }
}
