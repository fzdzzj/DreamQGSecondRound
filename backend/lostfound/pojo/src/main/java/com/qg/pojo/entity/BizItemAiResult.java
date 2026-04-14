package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 识别结果表
 */
@Data
@TableName("biz_item_ai_result")
public class BizItemAiResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 物品ID
     */
    private Long itemId;
    /**
     * 识别结果版本
     */
    private Integer resultVersion;
    /**
     * 源数据类型
     * 1:自动识别 2:重新识别
     */
    private String sourceType;
    /**
     * 源数据内容
     */
    private String originText;
    /**
     * 提示文本
     */
    private String promptText;
    /**
     * AI 描述
     */
    private String aiDescription;
    /**
     * AI 分类
     */
    private String aiCategory;
    /**
     * 图片URL列表
     */
    private List<String> imageUrls;
    /**
     * AI 模型名称
     */
    private String modelName;
    /**
     * 识别结果状态
     * SUCCESS/FAILURE
     */
    private String status;
    /**
     * 是否删除
     */
    private Integer isDeleted;
    /**
     * 创建人
     */
    private Long createUser;
    /**
     * 更新人
     */
    private Long updateUser;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
