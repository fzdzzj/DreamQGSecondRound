package com.qg.server.task;

import com.qg.server.service.RiskMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 风险扫描任务
 * 每30分钟执行一次
 * 扫描30分钟内的风险
 * 生成风险
 * 发送风险通知
 * 记录风险日志
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RiskScanTask {

    private final RiskMonitorService riskMonitorService;

    @Scheduled(cron = "0 0/30 * * * ?")
    public void scanPeriodicClusterRisk() {
        log.info("开始执行周期性风险扫描");
        riskMonitorService.scanPeriodicClusterRisk();
        log.info("完成周期性风险扫描");
    }
}
