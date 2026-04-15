package com.qg.server.controller.message;

import com.qg.common.context.BaseContext;
import com.qg.common.result.PageResult;
import com.qg.common.result.Result;
import com.qg.pojo.vo.NotificationVO;
import com.qg.pojo.vo.UnreadCountVO;
import com.qg.server.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 通知接口
 * 提供通知相关的接口，如获取用户未读通知数量、获取用户所有通知（分页）、标记通知为已读等
 */
@RestController
@RequestMapping("/notification")
@Tag(name = "通知接口")
@Slf4j
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    /**
     * 获取用户未读通知数量
     *
     * @return 用户未读通知数量
     */
    @GetMapping("/user/unread")
    @Operation(summary = "获取用户未读通知数量")
    public Result<UnreadCountVO> getUserUnreadNotificationCount() {
        Long userId = BaseContext.getCurrentId();
        log.info("查询用户未读通知数量，userId={}", userId);
        Long count = notificationService.getUserUnreadNotificationCount(userId);
        log.info("查询用户未读通知数量成功，userId={}, count={}", userId, count);
        return Result.success(new UnreadCountVO(count));
    }

    /**
     * 获取用户所有通知（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 通知列表分页结果
     */
    @GetMapping("/user")
    @Operation(summary = "获取用户所有通知")
    public Result<PageResult<NotificationVO>> getUserNotifications(@RequestParam(defaultValue = "1") int pageNum,
                                                                   @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = BaseContext.getCurrentId();
        log.info("查询用户所有通知，userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);
        PageResult<NotificationVO> pageResult = notificationService.getUserNotifications(userId, pageNum, pageSize);
        log.info("查询用户所有通知成功，userId={}, pageNum={}, pageSize={}, total={}",
                userId, pageNum, pageSize, pageResult.getTotal());
        return Result.success(pageResult);
    }

    /**
     * 标记通知为已读
     *
     * @param id 通知ID
     * @return 成功结果
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知为已读")
    public Result<Void> markNotificationAsRead(@PathVariable Long id) {
        log.info("用户请求标记通知为已读，id={}", id);
        notificationService.markNotificationAsRead(id);
        log.info("用户标记通知为已读成功，id={}", id);
        return Result.success();
    }

    /**
     * 删除通知（逻辑删除）
     *
     * @param id 通知ID
     * @return 成功结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        log.info("用户请求删除通知，id={}", id);
        notificationService.deleteNotification(id);
        log.info("用户删除通知成功，id={}", id);
        return Result.success();
    }

}
