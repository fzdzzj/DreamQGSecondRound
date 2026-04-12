package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "留言统计信息")
public class CommentStatVO {
    @Schema(description = "留言ID")
    private Long id;
    @Schema(description = "物品ID")
    private Long itemId;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "父评论ID")
    private Long parentId;
    @Schema(description = "评论内容")
    private String content;
    @Schema(description = "用户昵称")
    private String nickname;
    @Schema(description = "用户头像")
    private String avatar;
    @Schema(description = "是否已读,0:未读,1:已读")
    private Integer isRead;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 子评论
     */
    @Schema(description = "子评论列表")
    private List<CommentStatVO> children = new ArrayList<>();
}
