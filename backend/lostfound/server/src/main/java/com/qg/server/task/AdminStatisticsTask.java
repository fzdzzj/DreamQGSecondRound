package com.qg.server.task;

import com.qg.server.service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminStatisticsTask {
    private final AdminStatisticsService adminStatisticsService;

    @Scheduled(cron="0 0 1 * * ?")
    public void generateDailyAiReport(){
      log.info("开始生成日报");
      adminStatisticsService.generateDailyAiReport();
      log.info("日报生成完毕");
    }

    @Scheduled(cron="0 0 1 * * 1")
    public void generateWeeklyAiReport(){
      log.info("开始生成周报");
      adminStatisticsService.generateWeeklyAiReport();
      log.info("周报生成完毕");
    }

}
