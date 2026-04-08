package com.qg.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class CommentAddDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 物品ID
     */
    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    /**
     * 留言内容（包含联系方式）
     */
    @NotBlank(message = "留言内容不能为空")
    private String content;

    /**
     * 父留言ID，一级留言固定传0
     */
    private Long parentId;
}
