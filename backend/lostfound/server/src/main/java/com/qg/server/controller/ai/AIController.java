package com.qg.server.controller.ai;

import com.qg.common.context.BaseContext;
import com.qg.common.result.Result;
import com.qg.server.ai.repository.ChatHistoryRepository;
import com.qg.server.service.AiAsyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RestController
@RequestMapping("/ai")
@Tag(name = "AI接口", description = "AI接口")
@Slf4j
@RequiredArgsConstructor
public class AIController {
    private final AiAsyncService aiAsyncService;
    private final ChatClient answerChatClient;
    private final ChatHistoryRepository chatHistoryRepository;
    @PostMapping("/item/{itemId}/regenerate")
    @Operation(summary="重新生成物品AI描述")
    public Result<Void> regenerateItemDescription(@PathVariable Long itemId)
    {
        Long userId = BaseContext.getCurrentId();
        log.info("重新生成物品AI描述，用户ID={},物品ID={}", userId, itemId);
        aiAsyncService.regenerateItemDescription(itemId, userId);
        log.info("重新生成物品AI描述成功，用户ID={},物品ID={}", userId, itemId);
        return Result.success();
    }

    @RequestMapping(value="/service", produces="text/html;charset=utf-8")
    public Flux<String> service(String prompt, String chatId){
        Long userId = BaseContext.getCurrentId();
        // 保存用户输入
        chatHistoryRepository.saveMessage("answer", userId, chatId, "user", prompt);

        return answerChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream()
                .content()
                .doOnNext(reply -> {
                    // 保存AI回复
                    chatHistoryRepository.saveMessage("answer", userId, chatId, "assistant", reply);
                });
    }
}
