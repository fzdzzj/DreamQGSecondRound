package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Schema(description = "物品统计VO")
public class BizItemStatVO {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 类型: LOST(丢失), FOUND(拾取)
     */
    private String type;

    /**
     * 标题/物品名称
     */
    private String title;

    /**
     * 地点
     */
    private String location;

    /**
     * 丢失/拾取时间
     */
    private LocalDateTime happenTime;

    /**
     * 状态: OPEN, MATCHED, CLOSED, REPORTED, DELETED
     */
    private String status;

    /**
     * AI识别分类
     */
    private String aiCategory;

}
