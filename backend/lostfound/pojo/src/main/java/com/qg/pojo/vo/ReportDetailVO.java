package com.qg.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportDetailVO {

    private Long id;
    private Long itemId;
    private Long reporterId;

    private String reason;
    private String detail;

    private String statusCode;
    private String status;

    private Long adminId;
    private String auditRemark;
    private LocalDateTime auditTime;

    private LocalDateTime createTime;
}
