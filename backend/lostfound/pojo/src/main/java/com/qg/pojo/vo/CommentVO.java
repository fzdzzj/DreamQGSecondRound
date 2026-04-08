package com.qg.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CommentVO implements Serializable {
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
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 留言内容（包含联系方式）
     */
    private String content;

    /**
     * 父留言ID
     */
    private Long parentId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
