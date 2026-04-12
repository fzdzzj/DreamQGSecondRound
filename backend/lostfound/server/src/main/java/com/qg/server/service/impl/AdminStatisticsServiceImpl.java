package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.common.constant.BizItemStatusConstant;
import com.qg.common.constant.BizItemTypeConstant;
import com.qg.common.constant.MessageConstant;
import com.qg.common.properties.AIProperties;
import com.qg.pojo.entity.BizAiStatisticsReport;
import com.qg.pojo.entity.BizItem;
import com.qg.server.ai.client.AdminStatisticsAiClient;
import com.qg.server.mapper.BizAiStatisticsReportDao;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminStatisticsServiceImpl implements AdminStatisticsService {
    private final BizItemDao bizItemDao;
    private final BizAiStatisticsReportDao bizAiStatisticsReportDao;
    private final AdminStatisticsAiClient adminStatisticsAiClient;
    private final ObjectMapper objectMapper;
    private final AIProperties aiProperties;

    // 每日报表
    @Override
    public void generateDailyAiReport() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(1).atStartOfDay();
        LocalDateTime end = today.atStartOfDay();
        generateReport(today, start, end, "DAILY");
    }

    // 每周报表
    @Override
    public void generateWeeklyAiReport() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusWeeks(1).atStartOfDay();
        LocalDateTime end = today.atStartOfDay();
        generateReport(today, start, end, "WEEKLY");
    }
    @Override
    public void generateMonthlyAiReport() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusMonths(1).atStartOfDay();
        LocalDateTime end = today.atStartOfDay();
        generateReport(today, start, end, "MONTHLY");
    }

    /**
     * 统一生成报表（抽取所有重复逻辑）
     */
    private void generateReport(LocalDate statDate, LocalDateTime start, LocalDateTime end, String statType) {
        // 1. 查询数据
        List<BizItem> items = bizItemDao.selectList(
                new LambdaQueryWrapper<BizItem>()
                        .ge(BizItem::getCreateTime, start)
                        .le(BizItem::getCreateTime, end)
                        .eq(BizItem::getDeleted, 0)
        );

        // 2. 组装统计数据
        Map<String, Object> sourceData = new LinkedHashMap<>();
        sourceData.put("data", statDate.toString());
        sourceData.put("totalCount", items.size());
        sourceData.put("topLocations", topLocations(items));
        sourceData.put("topTypes", topTypes(items));
        sourceData.put("foundCount", countByType(items, BizItemTypeConstant.FOUND));
        sourceData.put("lostCount", countByType(items, BizItemTypeConstant.LOST));
        sourceData.put("closedCount", countByStatus(items, BizItemStatusConstant.CLOSED));

        // 3. 调用AI生成总结
        String prompt = buildPrompt(sourceData);
        String summary = adminStatisticsAiClient.generateSummary(prompt);

        // 4. 保存报表
        BizAiStatisticsReport report = new BizAiStatisticsReport();
        report.setStatDate(statDate);
        report.setStatType(statType);
        report.setSourceDataJson(toJson(sourceData));
        report.setAiSummary(summary);
        report.setModelName(aiProperties.getModel());
        if(summary.equals(MessageConstant.AI_GENERATE_FAILED)){
            report.setStatus("1");
        }else{
            report.setStatus("0");
        }

        bizAiStatisticsReportDao.insert(report);
    }

    // ========== 以下工具方法完全不变 ==========
    private List<Map<String, Object>> topLocations(List<BizItem> items) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getNormalizedLocation() == null || item.getNormalizedLocation().isBlank()
                                ? "未知"
                                : item.getNormalizedLocation(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("location", e.getKey());
                    map.put("count", e.getValue());
                    return map;
                })
                .toList();
    }

    private List<Map<String, Object>> topTypes(List<BizItem> items) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getTitle() == null || item.getTitle().isBlank() ? "未知物品" : item.getTitle(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("title", e.getKey());
                    map.put("count", e.getValue());
                    return map;
                })
                .toList();
    }

    private long countByType(List<BizItem> items, String type) {
        return items.stream().filter(i -> type.equals(i.getType())).count();
    }

    private long countByStatus(List<BizItem> items, String status) {
        return items.stream().filter(i -> status.equals(i.getStatus())).count();
    }

    private String buildPrompt(Map<String, Object> sourceData) {
        return """
                请根据以下校园失物招领平台统计数据，生成一段管理员分析总结：

                %s

                请输出：
                1. 主要现象总结
                2. 风险提醒
                3. 管理建议
                """.formatted(toJson(sourceData));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("统计数据转JSON失败", e);
            return "{}";
        }
    }
}