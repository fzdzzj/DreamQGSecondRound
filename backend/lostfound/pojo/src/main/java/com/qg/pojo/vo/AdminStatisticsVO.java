package com.qg.pojo.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdminStatisticsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 发布信息数量
     */
    private Long publishCount;

    /**
     * 找回物品数量
     */
    private Long foundCount;

    /**
     * 活跃用户数（近7天登录）
     */
    private Long activeUserCount;
}
