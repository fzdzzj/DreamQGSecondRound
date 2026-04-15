package com.qg.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 未读消息数VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnreadCountVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long count;
}