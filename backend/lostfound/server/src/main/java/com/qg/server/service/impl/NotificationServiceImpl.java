package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qg.pojo.entity.Notification;
import com.qg.server.mapper.NotificationDao;
import com.qg.server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationDao notificationDao;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createNotification(Long userId, Long commentId, String content) {
        log.info("生成通知，userId={}, commentId={}", userId, commentId);

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setCommentId(commentId);
        notification.setContent(content);
        notification.setIsRead(0); // 默认未读

        notificationDao.insert(notification);

        log.info("通知生成成功，notificationId={}", notification.getId());
    }

    @Override
    public Long getUserUnreadNotificationCount(Long userId) {
        return notificationDao.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)
        );
    }


}
