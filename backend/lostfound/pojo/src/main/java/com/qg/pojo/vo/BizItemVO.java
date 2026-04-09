package com.qg.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BizItemVO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String description;
    private String location;
    private LocalDateTime happenTime;
    private String statusDesc;
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
