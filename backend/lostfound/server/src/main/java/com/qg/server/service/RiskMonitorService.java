package com.qg.server.service;

import com.qg.pojo.entity.BizItem;

public interface RiskMonitorService {
    /**
     * 监控物品发布
     *
     * @param item
     */
    void onItemPublished(BizItem item);


    /**
     * 定期扫描物品风险
     */
    void scanPeriodicClusterRisk();

    void onItemFound(BizItem item);
}
