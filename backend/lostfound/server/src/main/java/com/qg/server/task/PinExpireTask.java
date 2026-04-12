package com.qg.server.task;

import com.qg.pojo.entity.BizItem;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PinExpireTask {
    private final ItemService itemService;

    @Scheduled(cron = "0 * * * * ?")
    public void clearExpiredPinnedItems(){
        log.info("开始清除过期的置顶物品");
        itemService.clearExpiredPinnedItems();
        log.info("清除过期的置顶物品完成");
    }
}
