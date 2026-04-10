package com.qg.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BizItemStatVO implements Serializable {
    private Long id;
    private String type;
    private String title;
    private String location;
    private LocalDateTime happenTime;
    private String status;
    private String statusDesc;
    private String aiCategory;
}
