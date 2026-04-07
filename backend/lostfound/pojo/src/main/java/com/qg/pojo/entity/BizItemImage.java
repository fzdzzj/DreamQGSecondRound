package com.qg.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_item_image")
public class BizItemImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long itemId;       // 关联物品ID（主表ID）
    private String url;        // 图片地址
    private LocalDateTime createTime;
}