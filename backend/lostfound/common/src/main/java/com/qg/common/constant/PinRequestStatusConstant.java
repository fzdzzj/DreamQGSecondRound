package com.qg.common.constant;

/**
 * 置顶申请状态
 */
public class PinRequestStatusConstant {

    private PinRequestStatusConstant() {
    }

    /**
     * 待审核
     */
    public static final String PENDING = "1";
    /**
     * 已审核
     */
    public static final String APPROVED = "2";
    /**
     * 已拒绝
     */
    public static final String REJECTED = "3";
    /**
     * 已取消
     */
    public static final String CANCELED = "4";

}
