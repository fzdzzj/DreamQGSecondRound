package com.qg.server.service;

import com.qg.pojo.entity.BizRiskEvent;

/**
 * 企业微信通知服务
 */
public interface WeComNotifyService {
    /**
     * 通知风险事件
     *
     * @param event 风险事件
     */
    void notifyRiskEvent(BizRiskEvent event);
}
