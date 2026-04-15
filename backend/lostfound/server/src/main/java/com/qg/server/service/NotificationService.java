package com.qg.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qg.common.result.PageResult;
import com.qg.pojo.entity.Notification;
import com.qg.pojo.vo.NotificationVO;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> { // 继承 IService

    /**
     * 创建通知
     *
     * @param userId    接收通知的用户ID
     * @param commentId 被回复的留言ID
     * @param content   通知内容
     */
    void createNotification(Long userId, Long commentId, String content);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    Long getUserUnreadNotificationCount(Long userId);

    /**
     * 获取用户所有通知（分页）
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 通知列表
     */
    PageResult<NotificationVO> getUserNotifications(Long userId, int pageNum, int pageSize);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     */
    void markNotificationAsRead(Long notificationId);

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     */
    void deleteNotification(Long notificationId);
}
