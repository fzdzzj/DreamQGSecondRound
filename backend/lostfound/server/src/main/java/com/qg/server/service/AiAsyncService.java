package com.qg.server.service;

public interface AiAsyncService {
    /**
     * 异步生成物品AI描述
     */
    void generateItemDescription(String title, String description, String location,Long userId,Long itemId);
}
