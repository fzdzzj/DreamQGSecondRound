package com.qg.server.controller;

import com.qg.common.context.BaseContext;
import com.qg.common.result.Result;
import com.qg.pojo.dto.CommentAddDTO;
import com.qg.server.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "留言接口")
public class CommentController {
    private final CommentService commentService;


    /**
     * 发表评论/留言
     */
    @PostMapping
    @Operation(summary="发表评论/留言")
    public Result<Void> addComment(@RequestBody CommentAddDTO commentAddDTO){
        Long userId= BaseContext.getCurrentId();
        log.info("用户发表留言，用户ID：{}，物品ID：{}",userId,commentAddDTO.getItemId());
        commentService.addComment(commentAddDTO);
        log.info("用户发表留言成功，用户ID：{}，物品ID：{}",userId,commentAddDTO.getItemId());
        return Result.success();
    }
}
