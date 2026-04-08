package com.qg.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PinRequestStatVO {

    private Long id;
    private Long itemId;
    private Long applicantId;

    private String statusDesc;
    private String status;

    private LocalDateTime createTime;
}
