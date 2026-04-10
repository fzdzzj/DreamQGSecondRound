package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "评论添加DTO")
public class CommentAddDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 物品ID
     */
    @Schema(description = "物品ID")
    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    /**
     * 留言内容（包含联系方式）
     */
    @Schema(description = "留言内容（包含联系方式）")
    @NotBlank(message = "留言内容不能为空")
    private String content;

    /**
     * 父留言ID，一级留言固定传0
     */
    @Schema(description = "父留言ID，一级留言固定传0")
    @NotNull(message = "父留言ID不能为空")
    private Long parentId;
}
