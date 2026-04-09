package com.qg.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知视图对象
 */
@Data
public class NotificationVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 被回复的留言ID
     */
    private Long commentId;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 是否已读：0未读 1已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
