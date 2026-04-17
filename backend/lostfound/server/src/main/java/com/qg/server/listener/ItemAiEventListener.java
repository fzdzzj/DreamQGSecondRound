package com.qg.server.listener;

import com.qg.common.properties.AIProperties;
import com.qg.server.ai.client.ImageDescriptionClient;
import com.qg.server.event.ItemAiGenerateEvent;
import com.qg.server.service.AiAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 物品AI事件监听器
 * 监听物品AI生成事件，根据事件类型调用AI服务生成物品描述或图片多模态描述
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ItemAiEventListener {

    private final AiAsyncService aiAsyncService;
    private final AIProperties aiProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 处理物品AI生成事件
     */
    @Async("aiTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleItemAiGenerateEvent(ItemAiGenerateEvent event) {
        log.info("收到物品AI生成事件, itemId={}", event.getItemId());

        try {
            // 执行AI任务 + 统一超时控制
            CompletableFuture.runAsync(() -> {
                        List<ImageDescriptionClient.ImageItem> images = event.getImageItems();

                        if (images == null || images.isEmpty()) {
                            // 纯文本生成
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
                    })
                    .orTimeout(aiProperties.getTimeoutMs(), TimeUnit.MILLISECONDS)
                    .join(); // join () = 等待任务结束 + 让超时生效 + 让异常能捕获

        } catch (Exception e) {
            if (e.getCause() instanceof TimeoutException) {
                redisTemplate.opsForValue().set("AI_TIMEOUT:" + event.getItemId(), "true", 1, TimeUnit.HOURS);
                log.error("AI任务超时（{}ms），itemId={}", aiProperties.getTimeoutMs(), event.getItemId());
            } else {
                log.error("AI处理异常，itemId={}", event.getItemId(), e);
            }
        }
    }
}