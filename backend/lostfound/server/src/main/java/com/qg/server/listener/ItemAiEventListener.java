package com.qg.server.listener;

import com.qg.server.event.ItemAiGenerateEvent;
import com.qg.server.service.AiAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItemAiEventListener {
    private final AiAsyncService aiAsyncService;

    @Async("aiTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleItemAiGenerateEvent(ItemAiGenerateEvent event){
        log.info("收到物品AI生成时,itemId={},triggerType={}",event.getItemId(),event.getTriggerType());
         aiAsyncService.generateItemDescription(event.getItemId(),event.getTriggerType());
    }

}
