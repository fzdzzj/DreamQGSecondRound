package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "物品统计VO")
public class BizItemStatVO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;

    /**
     * 类型: LOST / FOUND
     */
    private String type;

    /**
     * 标题
     */
    private String title;

    /**
     * 地点
     */
    private String location;

    /**
     * 时间
     */
    private LocalDateTime happenTime;

    /**
     * 状态code: OPEN
     */
    private String statusDesc;

    /**
     * 状态描述: 开放
     */
    private String status;

    /**
     * AI分类
     */
    private String aiCategory;
}
