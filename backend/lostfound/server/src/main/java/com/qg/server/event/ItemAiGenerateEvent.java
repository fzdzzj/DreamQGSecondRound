package com.qg.server.event;

import com.qg.server.ai.client.ImageDescriptionClient.ImageItem;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Collections;
import java.util.List;

/**
 * 物品AI生成事件
 */
@Getter
public class ItemAiGenerateEvent extends ApplicationEvent {

    /**
     * 物品ID
     */
    private final Long itemId;

    /**
     * 标题/物品名称
     */
    private final String title;

    /**
     * 用户描述
     */
    private final String description;

    /**
     * 丢失/拾取地点
     */
    private final String location;

    /**
     * 发布者ID
     */
    private final Long userId;

    /**
     * 图片列表，每个图片可带类型
     */
    private final List<ImageItem> imageItems;

    /**
     * 构造器
     */
    public ItemAiGenerateEvent(Object source,
                               Long itemId,
                               String title,
                               String description,
                               String location,
                               Long userId,
                               List<ImageItem> imageItems) {
        super(source);
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.userId = userId;
        this.imageItems = imageItems == null ? Collections.emptyList() : imageItems;
    }
}
