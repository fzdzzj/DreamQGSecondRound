package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "留言统计信息")
public class CommentStatVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 留言ID
     */
    @Schema(description = "留言ID")
    private Long id;

    /**
     * 物品ID
     */
    @Schema(description = "物品ID")
    private Long itemId;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickname;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像",nullable = true)
    private String avatar;

    /**
     * 留言内容（简短）
     */
    @Schema(description = "留言内容（简短）")
    private String content;

    /**
     * 留言时间
     */
    @Schema(description = "留言时间")
    private LocalDateTime createTime;

    /**
     * 是否已读
     */
    @Schema(description = "是否已读,0:未读,1:已读", example = "0")
    private Integer isRead;
}
