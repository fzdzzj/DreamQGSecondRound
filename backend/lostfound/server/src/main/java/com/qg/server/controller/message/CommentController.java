package com.qg.server.controller.message;

import com.qg.common.context.BaseContext;
import com.qg.common.result.PageResult;
import com.qg.common.result.Result;
import com.qg.pojo.dto.CommentAddDTO;
import com.qg.pojo.vo.CommentDetailVO;
import com.qg.pojo.vo.CommentStatVO;
import com.qg.pojo.vo.UnreadCountVO;
import com.qg.server.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 留言接口
 * 提供留言相关的接口，如发表留言、获取留言列表、删除留言等
 */
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "留言接口")
public class CommentController {
    private final CommentService commentService;


    /**
     * 发表评论/留言
     *
     * @param commentAddDTO 留言添加DTO
     * @return 发表留言结果
     */
    @PostMapping
    @Operation(summary = "发表评论/留言")
    public Result<Void> addComment(@RequestBody CommentAddDTO commentAddDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户发表留言，用户ID：{}，物品ID：{}", userId, commentAddDTO.getItemId());
        commentService.addComment(commentAddDTO);
        log.info("用户发表留言成功，用户ID：{}，物品ID：{}", userId, commentAddDTO.getItemId());
        return Result.success();
    }

    /**
     * 获取物品留言列表
     *
     * @param itemId 物品ID
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页查询结果
     */
    @GetMapping("/item/{itemId}")
    @Operation(summary = "获取物品留言列表")
    public Result<PageResult<CommentStatVO>> getCommentList(@PathVariable Long itemId,
                                                            @RequestParam(defaultValue = "1") int pageNum,
                                                            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("管理员请求查看留言列表，itemId={}, pageNum={}, pageSize={}", itemId, pageNum, pageSize);
        PageResult<CommentStatVO> pageResult = commentService.getCommentList(itemId, pageNum, pageSize);
        log.info("管理员查看留言列表成功，itemId={}, pageNum={}, pageSize={}", itemId, pageNum, pageSize);
        return Result.success(pageResult);
    }


    /**
     * 删除留言
     *
     * @param commentId 留言ID
     * @return 删除留言结果
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "删除留言")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户请求删除留言，commentId={}, userId={}", commentId, userId);
        commentService.deleteComment(commentId);
        log.info("用户删除留言成功，commentId={}, userId={}", commentId, userId);
        return Result.success();
    }

    /**
     * 获取留言详情
     *
     * @param commentId 留言ID
     * @return 留言详情
     */
    @GetMapping("/{commentId}")
    @Operation(summary = "获取留言详情")
    public Result<CommentDetailVO> getCommentDetail(@PathVariable Long commentId) {
        log.info("查询留言详情，commentId={}", commentId);
        CommentDetailVO commentVO = commentService.getCommentDetail(commentId);
        log.info("查询留言详情成功，commentId={} commentContent={}", commentId, commentVO.getContent());
        return Result.success(commentVO);
    }

    /**
     * 标记留言为已读
     *
     * @param commentId 留言ID
     * @return 标记留言为已读结果
     */
    @PutMapping("/{commentId}/read")
    @Operation(summary = "标记留言为已读")
    public Result<Void> markAsRead(@PathVariable Long commentId) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户请求标记留言为已读，commentId={}, userId={}", commentId, userId);
        commentService.markAsRead(commentId);
        log.info("用户标记留言为已读成功，commentId={}, userId={}", commentId, userId);
        return Result.success();
    }

    /**
     * 获取物品下未读留言数量
     *
     * @param itemId 物品ID
     * @return 未读留言数量
     */
    @GetMapping("/item/{itemId}/unread")
    @Operation(summary = "获取物品下未读留言数量")
    public Result<UnreadCountVO> getUnreadCount(@PathVariable("itemId") Long itemId) {
        log.info("查询物品下未读留言数量，itemId={}", itemId);
        Long count = commentService.getUnreadCount(itemId);
        log.info("查询物品下未读留言数量成功，itemId={}, count={}", itemId, count);
        return Result.success(new UnreadCountVO(count));
    }

    /**
     * 获取用户未读留言数量
     *
     * @return 用户未读留言数量
     */
    @GetMapping("/user/unread")
    @Operation(summary = "获取用户未读留言数量")
    public Result<UnreadCountVO> getUserUnreadCount() {
        Long userId = BaseContext.getCurrentId();
        log.info("查询用户未读留言数量，userId={}", userId);
        Long count = commentService.getUserUnreadCount(userId);
        log.info("查询用户未读留言数量成功，userId={}, count={}", userId, count);
        return Result.success(new UnreadCountVO(count));
    }

}
