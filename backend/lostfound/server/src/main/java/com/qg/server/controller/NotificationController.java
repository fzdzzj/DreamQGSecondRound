package com.qg.server.controller;

import com.qg.common.result.Result;
import com.qg.server.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
@Tag(name = "通知接口")
@Slf4j
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    /**
     * 获取用户未读通知数量
     */
    @GetMapping("/user/{userId}/unread")
    @Operation(summary = "获取用户未读通知数量")
    public Result<Long> getUserUnreadNotificationCount(@PathVariable Long userId) {
        log.info("查询用户未读通知数量，userId={}", userId);
        Long count = notificationService.getUserUnreadNotificationCount(userId);
        log.info("查询用户未读通知数量成功，userId={}, count={}", userId, count);
        return Result.success(count);
    }
}
