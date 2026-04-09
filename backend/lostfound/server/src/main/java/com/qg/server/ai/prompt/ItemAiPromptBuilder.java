package com.qg.server.ai.prompt;

import com.qg.common.constant.AiPromptConstant;
import com.qg.pojo.entity.BizItem;
import org.springframework.stereotype.Component;

@Component
public class ItemAiPromptBuilder {
    /**
     * 构建物品描述生成prompt
     */
    public String buildItemDescriptionPrompt(BizItem item){
        String title=safe(item.getTitle());
        String description=safe(item.getDescription());
        String location=safe(item.getLocation());
        return AiPromptConstant.ITEM_DESCRIPTION_PROMPT.formatted(title,description,location);
    }

    private String safe(String value){
        return value==null?"":value.trim();
    }
}
