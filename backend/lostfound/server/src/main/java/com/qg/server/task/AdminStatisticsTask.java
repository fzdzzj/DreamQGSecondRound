package com.qg.server.task;

import com.qg.server.service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 管理员统计任务
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminStatisticsTask {
    private final AdminStatisticsService adminStatisticsService;

    /**
     * 生成每日AI统计报表
     * 0 0 1 * * ? 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void generateDailyAiReport() {
        log.info("开始生成日报");
        adminStatisticsService.generateDailyAiReport();
        log.info("日报生成完毕");
    }

    /**
     * 生成每周AI统计报表
     * 0 0 2 * * 1 每周一凌晨2点执行
     */
    @Scheduled(cron = "0 0 2  * * 1")
    public void generateWeeklyAiReport() {
        log.info("开始生成周报");
        adminStatisticsService.generateWeeklyAiReport();
        log.info("周报生成完毕");
    }
    @Scheduled(cron = "0 0 0 1 * *")
    public void generateMonthlyAiReport() {
        log.info("开始生成月报");
        adminStatisticsService.generateMonthlyAiReport();
        log.info("月报生成完毕");
    }
}
