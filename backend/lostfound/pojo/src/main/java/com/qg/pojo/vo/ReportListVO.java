package com.qg.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportListVO {

    private Long id;
    private Long itemId;
    private Long reporterId;

    private String statusCode;
    private String status;

    private LocalDateTime createTime;
}
