package com.qg.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PinRequestListVO {

    private Long id;
    private Long itemId;
    private Long applicantId;

    private String statusCode;
    private String status;

    private LocalDateTime createTime;
}
