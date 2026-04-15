package com.qg.server.service;

import com.qg.server.ai.client.ImageDescriptionClient.ImageItem;

import java.util.List;
import java.util.Map;

/**
 * 异步处理 AI 描述服务
 */
public interface AiAsyncService {

    /**
     * 生成物品文本描述
     *
     * @param title       物品标题
     * @param description 物品描述
     * @param location    物品位置
     * @param userId      用户ID
     * @param itemId      物品ID
     */
    void generateItemDescription(String title, String description, String location, Long userId, Long itemId);

    /**
     * 多类型物品描述
     *
     * @param title       物品标题
     * @param description 物品描述
     * @param location    物品位置
     * @param userId      用户ID
     * @param itemId      物品ID
     * @param imageItems  图片描述列表
     */
    void generateItemImageDescription(String title,
                                      String description,
                                      String location,
                                      Long userId,
                                      Long itemId,
                                      List<ImageItem> imageItems);

    /**
     * 重新生成已有物品的 AI 描述
     *
     * @param itemId 物品ID
     * @param userId 用户ID
     */
    void regenerateItemDescription(Long itemId, Long userId);

    /**
     * 持久化 AI 结果到数据库
     *
     * @param title         物品标题
     * @param location      物品位置
     * @param generatedDesc 生成的描述
     * @param originDesc    原始描述
     * @param status        生成状态
     * @param aiCategory    描述状态
     * @return 新增结果 ID
     */
    Map<String, String> persistAiDescription(String title, String location, Long userId, Long itemId,
                                             String generatedDesc, String originDesc, String status,
                                             String aiCategory);

    /**
     * 更新 item 的 currentAiResultId
     *
     * @param itemId     物品ID
     * @param aiResultId AI 结果ID
     * @param aiStatus   AI 状态
     */
    void updateItemCurrentAiResultId(Long itemId, Long aiResultId, String aiStatus);
}
