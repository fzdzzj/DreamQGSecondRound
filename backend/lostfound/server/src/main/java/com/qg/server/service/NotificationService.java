package com.qg.server.service;

public interface NotificationService{
    /**
     * 创建通知
     *
     * @param userId 接收通知的用户ID
     * @param commentId 被回复的留言ID
     * @param content 通知内容
     */
    void createNotification(Long userId, Long commentId, String content);

    Long getUserUnreadNotificationCount(Long userId);
}
