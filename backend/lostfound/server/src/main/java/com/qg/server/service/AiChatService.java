package com.qg.server.service;

import reactor.core.publisher.Flux;
/**
 * AI SSE聊天服务接口
 */
public interface AiChatService {
    /**
     * 流式回答
     *
     * @param prompt 问题
     * @param chatId 会话ID
     * @param userId 用户ID
     * @return 回答流
     */
    Flux<String> streamAnswer(String prompt, String chatId, Long userId);

}
