package com.qg.common.constant;

/**
 * Redis 缓存常量类
 */
public class RedisConstant {
    private RedisConstant() {
    }

    /**
     * 详情缓存 key 前缀
     * 示例：item:detail:1001
     */
    public static final String ITEM_DETAIL_KEY = "item:detail:";

    /**
     * 分页缓存 key 前缀
     * 示例：ITEM_PAGE_KEY:1:10:LOST:校园卡:图书馆:1
     */
    public static final String ITEM_PAGE_KEY = "ITEM_PAGE_KEY:%d:%d:%s:%s:%s:%s";
    /**
     * 用户 AI 限制缓存 key 前缀
     */
    public static final String USER_AI_LIMIT_KEY = "user:ai:limit:";
    /**
     * 用户认领帖子数量限制缓存 key 前缀
     */
    public static final String USER_CLAIM_LIMIT_KEY = "user:claim:limit:";
    /**
     * 用户一分钟内创建的认领帖子数量限制
     */
    public static final Integer USER_CLAIM_POST_LIMIT = 5;
    /**
     * 用户一分钟内创建的编辑帖子数量限制缓存 key 前缀
     */
    public static final String USER_EDIT_POST_LIMIT_KEY = "user:edit:post:limit:";
    /**
     * 用户一分钟内创建的编辑帖子数量限制
     */
    public static final Integer USER_EDIT_POST_LIMIT = 5;
    /**
     * 用户一分钟内创建的帖子数量限制缓存 key 前缀
     */
    public static final String USER_POST_LIMIT_KEY = "user:post:limit:";
    /**
     * 用户一分钟内创建的帖子数量限制
     */
    public static final Integer USER_POST_LIMIT = 5;

    /**
     * 用户封禁缓存 key 前缀
     */
    public static final String USER_BANNED_KEY = "user:banned:";

    /**
     * 令牌黑名单缓存 key 前缀
     */
    public static final String TOKEN_BLACKLIST_KEY = "token:blacklist:";
}
