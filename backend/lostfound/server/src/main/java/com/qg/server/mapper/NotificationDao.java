package com.qg.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qg.pojo.entity.Notification;
import com.qg.pojo.vo.NotificationVO;

public interface NotificationDao extends BaseMapper<Notification> {

    /**
     * 获取用户所有通知
     *
     * @param page 分页信息
     * @param userId 用户ID
     * @return 用户的通知列表
     */
    Page<NotificationVO> selectUserNotifications(Page<NotificationVO> page, Long userId);
}

