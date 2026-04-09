package com.qg.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ReportListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long itemId;
    private Long reporterId;

    private String status;
    private String statusDesc;

    private LocalDateTime createTime;
}
