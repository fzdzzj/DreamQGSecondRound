package com.qg.server.service;

public interface AiAsyncService {
    /**
     * 异步生成物品AI描述
     */
    void generateItemDescription(Long itemId,String triggerType);
}
