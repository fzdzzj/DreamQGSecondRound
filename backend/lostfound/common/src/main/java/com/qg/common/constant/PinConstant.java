package com.qg.common.constant;

/**
 * 置顶状态常量
 * 0=未置顶 1=已置顶
 */
public class PinConstant {


    // 私有构造，防止实例化
    private PinConstant() {
    }

    /**
     * 未置顶
     */
    public static final Integer NOT_PINNED = 0;
    public static final long PIN_EXPIRE_HOURS = 24;
    /**
     * 已置顶
     */
    public static final Integer PINNED = 1;
}
