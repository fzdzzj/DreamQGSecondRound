package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("biz_item_ai_result")
public class BizItemAiResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long itemId; // 对应 BizItem
    private Integer resultVersion; // 版本号
    private String sourceType; // AUTO/REGENERATE
    private String originText; // 用户原始描述
    private String promptText; // AI Prompt
    private String aiDescription;
    private String aiCategory; // 多分类逗号分隔
    private List<String> imageUrls; // 多图片
    private String modelName;
    private String status; // SUCCESS/FAILURE
    private Integer isDeleted;
    private Long createUser;
    private Long updateUser;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
