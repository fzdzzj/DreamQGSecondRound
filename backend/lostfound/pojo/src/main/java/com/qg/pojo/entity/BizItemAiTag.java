package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BizItemAiTag {
    private Long id;
    private Long itemId;
    private Integer aiResultVersion;

    // ⚡ 改成 String 存 JSON
    private String aiTags;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
