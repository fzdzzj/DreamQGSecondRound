package com.qg.common.constant;

public class RedisConstant {
    /**
     * 详情缓存 key 前缀
     * 示例：item:detail:1001
     */
    public static final String ITEM_DETAIL_KEY = "item:detail:";

    /**
     * 分页缓存 key 前缀
     * 示例：item:page:type=LOST:keyword=校园卡:location=图书馆:page=1:size=10
     */
    public static final String ITEM_PAGE_KEY = "ITEM_PAGE_KEY:%d:%d:%s:%s:%s:%s";

    public static final String USER_AI_LIMIT_KEY = "user:ai:limit:";
}
