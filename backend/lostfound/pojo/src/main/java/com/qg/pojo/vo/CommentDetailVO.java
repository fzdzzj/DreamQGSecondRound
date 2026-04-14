package com.qg.pojo.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "留言详情VO")
public class CommentDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 物品ID
     */
    @Schema(description = "物品ID")
    private Long itemId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 留言内容（包含联系方式）
     */
    @Schema(description = "留言内容（包含联系方式）")
    private String content;

    /**
     * 父留言ID，一级留言固定传0
     */
    @Schema(description = "父留言ID，一级留言固定传0")
    private Long parentId;

    /**
     * 是否已读：0未读 1已读
     */
    @Schema(description = "是否已读：0未读 1已读")
    private Integer isRead;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String nickname;
    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String avatar;

    /**
     * 逻辑删除
     */
    @Schema(description = "逻辑删除")
    private Integer deleted;

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

