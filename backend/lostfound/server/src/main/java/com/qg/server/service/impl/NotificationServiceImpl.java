package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.MessageConstant;
import com.qg.common.exception.AbsentException;
import com.qg.common.result.PageResult;
import com.qg.pojo.entity.Notification;
import com.qg.pojo.vo.NotificationVO;
import com.qg.server.mapper.NotificationDao;
import com.qg.server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationDao, Notification> implements NotificationService {  // 继承 ServiceImpl 和实现 NotificationService

    private final NotificationDao notificationDao;  // 通知数据访问层

    /**
     * 创建通知
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createNotification(Long userId, Long commentId, String content) {
        log.info("生成通知，userId={}, commentId={}", userId, commentId);

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setCommentId(commentId);
        notification.setContent(content);
        notification.setIsRead(0); // 默认未读

        save(notification); // 使用 IService 提供的 save 方法

        log.info("通知生成成功，notificationId={}", notification.getId());
    }

    /**
     * 获取用户未读通知数量
     */
    @Override
    public Long getUserUnreadNotificationCount(Long userId) {
        return count(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
        );  // 使用 IService 提供的 count 方法
    }

    /**
     * 获取用户所有通知（分页）
     */
    @Override
    public PageResult<NotificationVO> getUserNotifications(Long userId, int pageNum, int pageSize) {
        log.info("查询用户所有通知，userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);

        Page<NotificationVO> page = new Page<>(pageNum, pageSize);

        // 查询通知
        Page<NotificationVO> notificationPage = notificationDao.selectUserNotifications(page, userId);

        return new PageResult<>(notificationPage.getRecords(), notificationPage.getTotal(), pageNum, pageSize);
    }

    /**
     * 标记通知为已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markNotificationAsRead(Long notificationId) {
        log.info("将通知标记为已读，notificationId={}", notificationId);

        Notification notification = getById(notificationId);  // 使用 IService 提供的 getById 方法
        if (notification == null) {
            log.warn("通知不存在，notificationId={}", notificationId);
            throw new AbsentException(MessageConstant.NOTIFICATION_NOT_FOUND);
        }

        // 标记为已读
        notification.setIsRead(1);
        updateById(notification);  // 使用 IService 提供的 updateById 方法

        log.info("通知标记为已读成功，notificationId={}", notificationId);
    }

    /**
     * 删除通知
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(Long notificationId) {
        log.info("删除通知，notificationId={}", notificationId);

        Notification notification = getById(notificationId);  // 使用 IService 提供的 getById 方法
        if (notification == null) {
            log.warn("通知不存在，notificationId={}", notificationId);
            throw new AbsentException(MessageConstant.NOTIFICATION_NOT_FOUND);
        }

        // 逻辑删除通知
        notification.setDeleted(1); // 设置已删除
        updateById(notification);  // 使用 IService 提供的 updateById 方法

        log.info("通知删除成功，notificationId={}", notificationId);
    }
}
