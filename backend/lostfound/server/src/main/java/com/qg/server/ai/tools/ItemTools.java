package com.qg.server.ai.tools;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizItemAiResult;
import com.qg.pojo.entity.BizItemAiTag;
import com.qg.server.mapper.BizItemAiResultDao;
import com.qg.server.mapper.BizItemAiTagDao;
import com.qg.server.mapper.BizItemDao;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemTools {

    private final BizItemDao bizItemDao;
    private final BizItemAiResultDao bizItemAiResultDao;
    private final BizItemAiTagDao bizItemAiTagDao;

    /**
     * 查询物品信息
     * @param name 物品名称
     * @param lostPlace 丢失地点，可选
     * @param itemIds 从AI结果和标签获得的物品ID列表，可选
     * @return 符合条件的物品列表
     */
    @Tool(description = "获取物品信息")
    public List<BizItem> queryItem(@ToolParam(description = "物品名称") String name,
                                   @ToolParam(description = "丢失地点，可选") String lostPlace,
                                   @ToolParam(description = "从查询AI结果和标签获得的物品ID") List<Long> itemIds) {

        QueryChainWrapper<BizItem> wrapper = new QueryChainWrapper<>(bizItemDao);

        // 名称匹配 title 或 description
        if (name != null && !name.isEmpty()) {
            wrapper.like(true, "title", name)
                    .or()
                    .like(true, "description", name);
        }

        // 丢失地点匹配 location
        if (lostPlace != null && !lostPlace.isEmpty()) {
            wrapper.or().like(true, "location", lostPlace);
        }

        // 如果同时有 AI 查询得到的 itemIds，也加上 in 条件
        if (itemIds != null && !itemIds.isEmpty()) {
            wrapper.or().in(true, "id", itemIds);
        }

        return wrapper.list();
    }

    /**
     * 查询物品的 AI 处理结果
     * @param description 物品概括描述
     * @return 对应物品ID列表
     */
    @Tool(description = "查询物品AI处理结果")
    public List<Long> queryAiResults(@ToolParam(description = "物品概括描述") String description) {

        if (description == null || description.isEmpty()) {
            return List.of();
        }

        List<BizItemAiResult> results = bizItemAiResultDao.selectList(
                new QueryChainWrapper<>(bizItemAiResultDao)
                        .like(true, "origin_text", description)
                        .or()
                        .like(true, "ai_category", description)
                        .or()
                        .like(true, "ai_description", description)
        );

        return results.stream()
                .map(BizItemAiResult::getItemId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 查询物品 AI 标签
     * @param description 物品概括描述
     * @param itemId 物品ID，可选
     * @return 对应物品ID列表
     */
    @Tool(description = "查询物品AI标签")
    public List<Long> queryAiTags(@ToolParam(description = "物品概括描述") String description,
                                  @ToolParam(description = "物品ID，可选") Long itemId) {

        QueryChainWrapper<BizItemAiTag> wrapper = new QueryChainWrapper<>(bizItemAiTagDao);

        if (itemId != null) {
            wrapper.eq(true, "item_id", itemId);
        }

        if (description != null && !description.isEmpty()) {
            wrapper.or().like(true, "tag", description);
        }

        List<BizItemAiTag> tags = wrapper.list();

        return tags.stream()
                .map(BizItemAiTag::getItemId)
                .distinct()
                .collect(Collectors.toList());
    }
}
