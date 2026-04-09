package com.qg.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ReportDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long itemId;
    private Long reporterId;

    private String reason;
    private String detail;

    private String statusDesc;
    private String status;

    private Long adminId;
    private String auditRemark;
    private LocalDateTime auditTime;

    private LocalDateTime createTime;
}
