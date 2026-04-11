package com.qg.server.controller.message;

import com.qg.common.context.BaseContext;
import com.qg.common.result.PageResult;
import com.qg.common.result.Result;
import com.qg.pojo.dto.CommentAddDTO;
import com.qg.pojo.vo.CommentDetailVO;
import com.qg.pojo.vo.CommentStatVO;
import com.qg.server.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 获取物品留言列表
     */
    @GetMapping("/item/{id}")
    @Operation(summary = "获取物品留言列表")
    public Result<PageResult<CommentStatVO>> getCommentList(@PathVariable Long id,
                                                            @RequestParam(defaultValue = "1") int pageNum,
                                                            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("管理员请求查看留言列表，id={}, pageNum={}, pageSize={}", id, pageNum, pageSize);
        PageResult<CommentStatVO> pageResult = commentService.getCommentList(id, pageNum, pageSize);
        log.info("管理员查看留言列表成功，id={}, pageNum={}, pageSize={}", id, pageNum, pageSize);
        return Result.success(pageResult);
    }


    /**
     * 删除留言
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除留言")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户请求删除留言，id={}, userId={}", id, userId);
        commentService.deleteComment(id);
        log.info("用户删除留言成功，id={}, userId={}", id, userId);
        return Result.success();
    }
    /**
     * 获取留言详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取留言详情")
    public Result<CommentDetailVO> getCommentDetail(@PathVariable Long id) {
        log.info("查询留言详情，id={}", id);
        CommentDetailVO commentVO = commentService.getCommentDetail(id);
        log.info("查询留言详情成功，id={}", id);
        return Result.success(commentVO);
    }
    /**
     * 标记留言为已读
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记留言为已读")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户请求标记留言为已读，id={}, userId={}", id, userId);
        commentService.markAsRead(id);
        log.info("用户标记留言为已读成功，id={}, userId={}", id, userId);
        return Result.success();
    }

    /**
     * 获取物品下未读留言数量
     */
    @GetMapping("/item/{id}/unread")
    @Operation(summary = "获取物品下未读留言数量")
    public Result<Long> getUnreadCount(@PathVariable Long id) {
        log.info("查询物品下未读留言数量，id={}", id);
        Long count = commentService.getUnreadCount(id);
        log.info("查询物品下未读留言数量成功，id={}, count={}", id, count);
        return Result.success(count);
    }

    /**
     * 获取用户未读留言数量
     */
    @GetMapping("/user/unread")
    @Operation(summary = "获取用户未读留言数量")
    public Result<Long> getUserUnreadCount() {
        Long userId = BaseContext.getCurrentId();
        log.info("查询用户未读留言数量，userId={}", userId);
        Long count = commentService.getUserUnreadCount(userId);
        log.info("查询用户未读留言数量成功，userId={}, count={}", userId, count);
        return Result.success(count);
    }

    /**
     * 回复留言
     */
    @PostMapping("/reply")
    @Operation(summary = "回复留言")
    public Result<Void> replyComment(@Validated @RequestBody CommentAddDTO commentAddDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户请求回复留言，userId={}, itemId={}, parentId={}", userId, commentAddDTO.getItemId(), commentAddDTO.getParentId());
        commentService.replyComment(commentAddDTO);
        log.info("用户回复留言成功，userId={}, itemId={}, parentId={}", userId, commentAddDTO.getItemId(), commentAddDTO.getParentId());
        return Result.success();
    }






}
