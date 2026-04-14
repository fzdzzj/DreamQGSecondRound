package com.qg.common.constant;

/**
 * 认领请求状态常量类
 */
public class BizClaimRequestStatusConstant {
    private BizClaimRequestStatusConstant() {
    }

    /**
     * 待处理
     */
    public static final String PENDING = "1";
    /**
     * 已处理
     */
    public static final String APPROVED = "2";
    /**
     * 拒绝
     */
    public static final String REJECTED = "3";
    /**
     * 需要更多信息
     */
    public static final String MORE_INFO_REQUIRED = "4";
}
