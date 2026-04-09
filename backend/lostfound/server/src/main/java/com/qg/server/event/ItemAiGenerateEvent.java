package com.qg.server.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
@Getter
public class ItemAiGenerateEvent extends ApplicationEvent {
    /**
     * 物品ID
     */
    private final Long itemId;
    private final String title;
    private final String description;
    private final String location;
    private final Long userId;

    /**
     * 触发类型：CREATE/UPDATE/REGENERATE
     */
    public ItemAiGenerateEvent(Object source,Long itemId,String title,String description,String location,Long userId){
        super(source);
        this.itemId=itemId;
        this.title=title;
        this.description=description;
        this.location=location;
        this.userId=userId;
    }
}
