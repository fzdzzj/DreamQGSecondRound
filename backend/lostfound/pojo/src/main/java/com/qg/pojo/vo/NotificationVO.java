package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知视图对象
 */
@Data
@Schema(description = "通知视图对象")
public class NotificationVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @Schema(description = "通知ID")
    private Long id;

    /**
     * 被回复的留言ID
     */
    @Schema(description = "被回复的留言ID")
    private Long commentId;

    /**
     * 通知内容
     */
    @Schema(description = "通知内容")
    private String content;

    /**
     * 是否已读：0未读 1已读
     */
    @Schema(description = "是否已读：0未读 1已读")
    private Integer isRead;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
