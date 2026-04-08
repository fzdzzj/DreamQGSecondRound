package com.qg.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BizItemVO {

    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String description;
    private String location;
    private LocalDateTime happenTime;
    private String statusCode;
    private String status;
    private Integer isPinned;
    private LocalDateTime pinExpireTime;
    private String contactMethod;
    private String aiCategory;
    private String aiTags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 图片URL列表
     */
    private List<String> imageUrls;
}
