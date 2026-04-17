package com.qg.server.task;

import com.qg.server.service.ItemService;
import com.qg.server.service.PinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 自动清除过期置顶物品任务
 * 每30分钟执行一次
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PinTask {
    private final ItemService itemService;
    private final PinService pinService;

    /**
     * 每30分钟执行一次
     * 清除过期的置顶物品
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void clearExpiredPinnedItems() {
        log.info("开始清除过期的置顶物品");
        itemService.clearExpiredPinnedItems();
        log.info("清除过期的置顶物品完成");
    }
    @Scheduled(cron = "0 0 0 * * ?")
    public void clearExpiredPinnedItemsDaily() {
        log.info("开始清除过时的置顶物品");
        pinService.clearPinRequests();
        log.info("清除过时的置顶物品完成");
    }
}
