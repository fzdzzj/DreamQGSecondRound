package com.qg.server.service;

/**
 * 管理员统计服务
 */
public interface AdminStatisticsService {
    /**
     * 生成每日AI统计报表
     */
    void generateDailyAiReport();

    /**
     * 获取每日AI统计报表
     */
    void generateWeeklyAiReport();

    void generateMonthlyAiReport();
}
