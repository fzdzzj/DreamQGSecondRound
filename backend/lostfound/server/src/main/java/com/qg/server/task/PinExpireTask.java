package com.qg.server.task;

import com.qg.server.service.ItemService;
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
public class PinExpireTask {
    private final ItemService itemService;

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
}
