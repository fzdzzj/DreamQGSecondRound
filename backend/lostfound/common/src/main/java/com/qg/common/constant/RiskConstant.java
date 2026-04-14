package com.qg.common.constant;

/**
 * 风险常量类
 */
public class RiskConstant {
    private RiskConstant() {
    }

    /**
     * 风险类型
     */
    public static final String RISK_TYPE_HIGH_VALUE_ITEM = "1";
    public static final String RISK_TYPE_PERIODIC_CLUSTER = "2";
    public static final String RISK_TYPE_SENSITIVE_ITEM = "3";
    public static final String RISK_TYPE_ITEM_FOUND = "4";
    /**
     * 风险等级
     */
    public static final String RISK_LEVEL_LOW = "1";
    public static final String RISK_LEVEL_MEDIUM = "2";
    public static final String RISK_LEVEL_HIGH = "3";
    public static final String RISK_LEVEL_CRITICAL = "4";

    /**
     * 通知状态
     */
    public static final String NOTIFY_STATUS_PENDING = "1";
    public static final String NOTIFY_STATUS_SUCCESS = "2";
    public static final String NOTIFY_STATUS_FAIL = "3";

    /**
     * 处理状态
     */
    public static final String RISK_LEVEL_NO = "0";
    public static final String HANDLE_STATUS_UNHANDLED = "1";
    public static final String HANDLE_STATUS_RESOLVED = "2";
    public static final String HANDLE_STATUS_IGNORED = "3";
}
