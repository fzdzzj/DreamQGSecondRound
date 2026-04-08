package com.qg.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PinRequestDetailVO {

    private Long id;
    private Long itemId;
    private Long applicantId;

    private String reason;

    private String statusDesc;
    private String status;

    private Long auditAdminId;
    private String auditRemark;
    private LocalDateTime auditTime;

    private LocalDateTime createTime;
}
