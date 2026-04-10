package com.qg.server.service;

import com.qg.pojo.vo.ImageAiResponseVO;
import com.qg.server.ai.client.ImageDescriptionClient.ImageItem;

import java.util.List;

public interface AiAsyncService {

    /**
     * 生成物品文本描述
     */
    void generateItemDescription(String title, String description, String location, Long userId, Long itemId);

    /**
     * 生成多图、多类型物品描述
     */
    void generateItemImageDescription(String title,
                                      String description,
                                      String location,
                                      Long userId,
                                      Long itemId,
                                      List<ImageItem> imageItems);

    /**
     * 持久化 AI 结果到数据库
     * @return 新增结果 ID
     */
    Long persistAiDescription(String title,
                              String location,
                              Long userId,
                              Long itemId,
                              String generatedDesc,
                              String originDesc,
                              String status);

    /**
     * 更新 item 的 currentAiResultId
     */
    void updateItemCurrentAiResultId(Long itemId, Long aiResultId);
}
