package com.qg.server.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class ItemAiGenerateEvent extends ApplicationEvent {
    /**
     * 物品ID
     */
    private final Long itemId;

    /**
     * 触发类型：CREATE/UPDATE/REGENERATE
     */
    private final String triggerType;
    public ItemAiGenerateEvent(Object source,Long itemId,String triggerType){
        super(source);
        this.itemId=itemId;
        this.triggerType=triggerType;
    }
}
