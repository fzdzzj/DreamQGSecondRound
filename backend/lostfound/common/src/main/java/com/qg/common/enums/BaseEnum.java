package com.qg.common.enums;

/**
 * 所有数据库枚举的基础接口
 * code: 存数据库
 * desc: 显示给前端
 */
public interface BaseEnum {

    /**
     * 存数据库的值
     */
    String getCode();

    /**
     * 展示值
     */
    String getDesc();
}
