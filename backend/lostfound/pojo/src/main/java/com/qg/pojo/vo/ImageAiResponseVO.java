package com.qg.pojo.vo;

import lombok.Data;

@Data
public class ImageAiResponseVO {
    private String aiCategory;    // AI 生成分类
    private String aiTags;        // AI 生成标签
    private String aiDescription; // 可选：辅助生成描述
    private String status;        // SUCCESS / FAILED
}
