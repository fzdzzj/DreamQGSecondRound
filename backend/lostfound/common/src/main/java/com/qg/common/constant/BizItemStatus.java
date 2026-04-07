package com.qg.common.constant;

/**
 * 物品状态常量
 */
public class BizItemStatus {

    private BizItemStatus() {
    }

    /**
     * 开放中：可公开浏览、可检索
     */
    public static final String OPEN = "OPEN";

    /**
     * 已匹配：已找到可能对应的失主/拾主
     */
    public static final String MATCHED = "MATCHED";

    /**
     * 已关闭：流程结束，不再公开参与流转
     */
    public static final String CLOSED = "CLOSED";

    /**
     * 已举报：被举报后进入人工审核或隐藏状态
     */
    public static final String REPORTED = "REPORTED";

    /**
     * 已删除：逻辑删除状态
     */
    public static final String DELETED = "DELETED";
}
