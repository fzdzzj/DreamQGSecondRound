package com.qg.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BizItemDetailVO implements Serializable {
    private Long id;
    private String type;
    private String title;
    private String description;
    private String location;
    private LocalDateTime happenTime;
    private String status;
    private String statusDesc;
    private List<String> imageUrls;

    private String aiStatus;
    private String aiCategory;
    private String aiTags;
    private String aiDescription;
}
