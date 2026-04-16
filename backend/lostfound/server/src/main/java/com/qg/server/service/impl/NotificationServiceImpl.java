package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qg.common.constant.DefaultPageConstant;
import com.qg.common.constant.DeletedConstant;
import com.qg.common.constant.MessageConstant;
import com.qg.common.constant.ReadStatusConstant;
import com.qg.common.context.BaseContext;
import com.qg.common.exception.AbsentException;
import com.qg.common.result.PageResult;
import com.qg.pojo.entity.Notification;
import com.qg.pojo.vo.NotificationVO;
import com.qg.server.mapper.NotificationDao;
import com.qg.server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 通知服务实现类
 * 提供通知相关的业务逻辑实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationDao, Notification> implements NotificationService {  // 继承 ServiceImpl 和实现 NotificationService

    private final NotificationDao notificationDao;

    /**
     * 创建通知
     */
    @Override
    public void createNotification(Long userId, Long commentId, String content) {
        log.info("生成通知，userId={}, commentId={}", userId, commentId);

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setCommentId(commentId);
        notification.setContent(content);
        notification.setIsRead(ReadStatusConstant.UNREAD); // 默认未读

        save(notification);

        log.info("通知生成成功，notificationId={}", notification.getId());
    }

    /**
     * 获取用户未读通知数量
     */
    @Override
    public Long getUserUnreadNotificationCount(Long userId) {
        log.info("查询用户未读通知数量，userId={}", userId);
        Long count = count(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, ReadStatusConstant.UNREAD)
        );
        log.info("用户未读通知数量为：{}", count);
        return count;
    }

    /**
     * 获取用户所有通知（分页）
     */
    @Override
    public PageResult<NotificationVO> getUserNotifications(Long userId, int pageNum, int pageSize) {
        log.info("查询用户所有通知，userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);
        if (pageNum <= 0) {
            pageNum = DefaultPageConstant.DEFAULT_PAGE_NUM;
        }
        if (pageSize <= 0) {
            pageSize = DefaultPageConstant.DEFAULT_PAGE_SIZE;
        }
        Page<NotificationVO> page = new Page<>(pageNum, pageSize);

        // 查询通知
        Page<NotificationVO> notificationPage = notificationDao.selectUserNotifications(page, userId);
        log.info("用户所有通知数量为：{}", notificationPage.getTotal());
        return new PageResult<>(notificationPage.getRecords(), notificationPage.getTotal(), pageNum, pageSize);
    }

    /**
     * 标记通知为已读
     * 1. 查询通知是否存在
     * 2. 标记为已读
     * 3. 更新通知状态
     */
    @Override
    public void markNotificationAsRead(Long notificationId) {
        log.info("将通知标记为已读，notificationId={}", notificationId);
        // 1 查询通知是否存在
        Notification notification = getById(notificationId);
        if (notification == null) {
            log.warn("通知不存在，notificationId={}", notificationId);
            throw new AbsentException(MessageConstant.NOTIFICATION_NOT_FOUND);
        }

        //2 标记为已读
        notification.setIsRead(ReadStatusConstant.READ);
        updateById(notification);  // 使用 IService 提供的 updateById 方法

        log.info("通知标记为已读成功，notificationId={}", notificationId);
    }

    /**
     * 删除通知
     * 1. 查询通知是否存在
     * 2. 校验用户是否有权限删除该通知
     * 3. 逻辑删除通知
     * 4. 更新通知状态
     */
    @Override
    public void deleteNotification(Long notificationId) {
        log.info("删除通知，notificationId={}", notificationId);
        // 1 查询通知是否存在
        Notification notification = getById(notificationId);
        if (notification == null) {
            log.warn("通知不存在，notificationId={}", notificationId);
            throw new AbsentException(MessageConstant.NOTIFICATION_NOT_FOUND);
        }
        // 2 校验用户是否有权限删除该通知
        if (!notification.getUserId().equals(BaseContext.getCurrentId())) {
            log.warn("用户无权限删除该通知，notificationId={}", notificationId);
            throw new AbsentException(MessageConstant.NO_PERMISSION);
        }

        notificationDao.deleteById(notificationId);

        log.info("通知删除成功，notificationId={}", notificationId);
    }
}
