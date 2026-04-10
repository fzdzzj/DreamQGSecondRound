package com.qg.common.constant;

/**
 * 举报状态常量
 */
public class ReportStatus {

    private ReportStatus() {
    }

    /**
     * 待审核
     */
    public static final String PENDING = "1";

    /**
     * 审核通过
     */
    public static final String APPROVED = "2";

    /**
     * 审核驳回
     */
    public static final String REJECTED = "3";
}
