package com.qg.server.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizItemAiResult;
import com.qg.pojo.entity.BizItemAiTag;
import com.qg.server.mapper.BizItemAiResultDao;
import com.qg.server.mapper.BizItemAiTagDao;
import com.qg.server.mapper.BizItemDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemTools {

    private final BizItemDao bizItemDao;
    private final BizItemAiResultDao bizItemAiResultDao;
    private final BizItemAiTagDao bizItemAiTagDao;

    /**
     * 查询物品信息
     *
     * @param name      物品名称，可选
     * @param lostPlace 丢失地点，可选
     * @param itemIds   从 AI 结果和标签获得的物品 ID 列表，可选
     * @return 符合条件的物品列表
     */
    @Tool(description = "获取物品信息")
    public List<BizItem> queryItem(
            @ToolParam(description = "物品名称，可选") String name,
            @ToolParam(description = "丢失地点，可选") String lostPlace,
            @ToolParam(description = "从查询AI结果和标签获得的物品ID列表，可选") List<Long> itemIds) {
        // 1.创建查询条件
        LambdaQueryWrapper<BizItem> wrapper = Wrappers.lambdaQuery();

        boolean hasName = hasText(name);
        boolean hasLostPlace = hasText(lostPlace);
        boolean hasItemIds = itemIds != null && !itemIds.isEmpty();

        if (hasName) {
            String keyword = name.trim();
            wrapper.and(w -> w.like(BizItem::getTitle, keyword)
                    .or()
                    .like(BizItem::getDescription, keyword));
        }

        if (hasLostPlace) {
            wrapper.like(BizItem::getLocation, lostPlace.trim());
        }

        if (hasItemIds) {
            wrapper.in(BizItem::getId, itemIds);
        }
        // 2.执行查询
        List<BizItem> items = bizItemDao.selectList(wrapper);
        log.info("查询物品信息成功，name={}, lostPlace={}, itemIds={}, count={}",
                name, lostPlace, itemIds, items.size());
        // 3.返回结果
        return items;
    }


    /**
     * 查询物品的 AI 处理结果
     *
     * @param description 物品概括描述
     * @return 对应物品 ID 列表
     *
     * 1.创建查询条件
     * 2.执行查询
     * 3.返回结果
     */
    @Tool(description = "查询物品AI处理结果")
    public List<Long> queryAiResults(
            @ToolParam(description = "物品概括描述") String description) {
        // 1.创建查询条件
        if (!hasText(description)) {
            return Collections.emptyList();
        }

        String keyword = description.trim();
        log.info("查询物品AI处理结果，物品概括描述={}", keyword);

        LambdaQueryWrapper<BizItemAiResult> wrapper = Wrappers.lambdaQuery();
        wrapper.and(w -> w.like(BizItemAiResult::getOriginText, keyword)
                .or()
                .like(BizItemAiResult::getAiCategory, keyword)
                .or()
                .like(BizItemAiResult::getAiDescription, keyword));
        // 2.执行查询
        List<BizItemAiResult> results = bizItemAiResultDao.selectList(wrapper);
        // 3.返回结果
        List<Long> itemIds = results.stream()
                .map(BizItemAiResult::getItemId)
                .distinct()
                .collect(Collectors.toList());

        log.info("查询物品AI处理结果成功，description={}, itemIds={}", keyword, itemIds);
        return itemIds;
    }


    /**
     * 查询物品 AI 标签
     *
     * @param description 物品概括描述，可选
     * @param itemIds     物品ID列表，可选
     * @return 对应物品 ID 列表
     *
     * 1.创建查询条件
     * 2.执行查询
     * 3.返回结果
     */
    @Tool(description = "查询物品AI标签")
    public List<Long> queryAiTags(
            @ToolParam(description = "物品概括描述，可选") String description,
            @ToolParam(description = "物品ID列表，可选") List<Long> itemIds) {
        // 1.创建查询条件
        LambdaQueryWrapper<BizItemAiTag> wrapper = Wrappers.lambdaQuery();
        // 判断是否有查询条件
        boolean hasDescription = hasText(description);
        boolean hasItemIds = itemIds != null && !itemIds.isEmpty();
        // 如果有物品ID列表,则添加物品ID查询条件
        if (hasItemIds) {
            wrapper.in(BizItemAiTag::getItemId, itemIds);
        }
        // 如果有物品概括描述,则添加物品概括描述查询条件
        if (hasDescription) {
            wrapper.like(BizItemAiTag::getAiTags, description.trim());
        }
        // 2.执行查询
        List<BizItemAiTag> tags = bizItemAiTagDao.selectList(wrapper);
        // 3.返回结果
        log.info("查询物品AI标签成功，itemIds={}, description={}, tags={}",
                itemIds, description, tags);

        return tags.stream()
                .map(BizItemAiTag::getItemId)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean hasText(String text) {
        return text != null && !text.trim().isEmpty();
    }
}
