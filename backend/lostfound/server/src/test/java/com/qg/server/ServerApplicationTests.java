package com.qg.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.server.config.JacksonConfig;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.qg.common.constant.BizItemStatus;
import com.qg.common.constant.BizItemType;
import com.qg.common.util.PasswordUtil;
import com.qg.pojo.dto.LostBizItemDTO;
import com.qg.pojo.entity.BizItem;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.service.ItemService;
import com.qg.server.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

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
        item.setAiStatus("PENDING");

        BizItem savedItem = itemService.publishLostItem(item);
        System.out.println("生成的 ID：" + savedItem.getId());
    }
}
