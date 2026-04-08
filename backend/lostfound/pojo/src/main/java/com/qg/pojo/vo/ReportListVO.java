package com.qg.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportListVO {

    private Long id;
    private Long itemId;
    private Long reporterId;

    private String status;
    private String statusDesc;

    private LocalDateTime createTime;
}
