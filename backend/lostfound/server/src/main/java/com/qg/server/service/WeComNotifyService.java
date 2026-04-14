package com.qg.server.service;

import com.qg.pojo.entity.BizRiskEvent;

public interface WeComNotifyService {
    void notifyRiskEvent(BizRiskEvent event);
}
