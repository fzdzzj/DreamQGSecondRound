package com.qg.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CommentStatVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 留言ID
     */
    private Long id;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 留言内容（简短）
     */
    private String content;

    /**
     * 留言时间
     */
    private LocalDateTime createTime;

    /**
     * 是否已读
     */
    private Integer isRead;
}
