package com.qg.server.service;

public interface AdminStatisticsService {

    void generateDailyAiReport();

    void generateWeeklyAiReport();

    void generateMonthlyAiReport();
}
