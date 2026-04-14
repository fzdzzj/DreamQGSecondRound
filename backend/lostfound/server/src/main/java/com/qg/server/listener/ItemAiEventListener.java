package com.qg.server.listener;

import com.qg.server.ai.client.ImageDescriptionClient.ImageItem;
import com.qg.server.event.ItemAiGenerateEvent;
import com.qg.server.service.AiAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * 物品AI事件监听器
 * 监听物品AI生成事件，根据事件类型调用AI服务生成物品描述或图片多模态描述
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ItemAiEventListener {

    private final AiAsyncService aiAsyncService;

    /**
     * 处理物品AI生成事件
     *
     * @param event 物品AI生成事件
     */
    @Async("aiTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleItemAiGenerateEvent(ItemAiGenerateEvent event) {
        log.info("收到物品AI生成事件, itemId={}", event.getItemId());

        List<ImageItem> images = event.getImageItems();

        if (images == null || images.isEmpty()) {
            // 文本生成
            aiAsyncService.generateItemDescription(
                    event.getTitle(),
                    event.getDescription(),
                    event.getLocation(),
                    event.getUserId(),
                    event.getItemId()
            );
        } else {
            // 图片多模态生成
            aiAsyncService.generateItemImageDescription(
                    event.getTitle(),
                    event.getDescription(),
                    event.getLocation(),
                    event.getUserId(),
                    event.getItemId(),
                    images
            );
        }
    }

}
