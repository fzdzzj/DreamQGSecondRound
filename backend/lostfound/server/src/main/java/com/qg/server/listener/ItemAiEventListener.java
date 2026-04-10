package com.qg.server.listener;

import com.qg.server.ai.client.DescriptionClient;
import com.qg.server.ai.client.ImageDescriptionClient;
import com.qg.server.ai.client.ImageDescriptionClient.ImageItem;
import com.qg.server.event.ItemAiGenerateEvent;
import com.qg.server.service.AiAsyncService;
import com.qg.pojo.vo.ImageAiResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItemAiEventListener {

    private final AiAsyncService aiAsyncService;

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
