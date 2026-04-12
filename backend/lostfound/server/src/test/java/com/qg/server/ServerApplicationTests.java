package com.qg.server;

import com.qg.common.constant.BizItemAiResultStatusConstant;
import com.qg.pojo.entity.BizItem;
import com.qg.server.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class ServerApplicationTests {
    @Autowired
    private ItemServiceImpl itemService;

    @Test
    void testPublishLostItem() {
        BizItem item = new BizItem();
        item.setUserId(1L);
        item.setType("LOST");
        item.setTitle("Test Lost Item");
        item.setDescription("Test description");
        item.setLocation("Library");
        item.setHappenTime(LocalDateTime.now());
        item.setStatus("OPEN");
        item.setIsPinned(0);
        item.setAiStatus(BizItemAiResultStatusConstant.PENDING);

        BizItem savedItem = itemService.publishLostItem(item);
        System.out.println("生成的 ID：" + savedItem.getId());
    }
}
