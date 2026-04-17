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

/**
 * 管理员统计服务实现类
 * 负责生成每日、每周、每月的AI统计报表
 */
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
        generateReport(today, start, end, "1");
    }

    // 每周报表
    @Override
    public void generateWeeklyAiReport() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusWeeks(1).atStartOfDay();
        LocalDateTime end = today.atStartOfDay();
        generateReport(today, start, end, "2");
    }

    @Override
    public void generateMonthlyAiReport() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusMonths(1).atStartOfDay();
        LocalDateTime end = today.atStartOfDay();
        generateReport(today, start, end, "3");
    }

    /**
     * 统一生成报表（抽取所有重复逻辑）
     *
     * @param statDate 统计日期
     * @param start    开始时间
     * @param end      结束时间
     * @param statType 统计类型（DAILY、WEEKLY、MONTHLY）
     *                 1. 查询数据
     *                 2. 组装统计数据
     *                 3. 调用AI生成总结
     *                 4. 保存报表
     */
    private void generateReport(LocalDate statDate, LocalDateTime start, LocalDateTime end, String statType) {
        // 1. 查询数据
        List<BizItem> items = bizItemDao.selectList(
                new LambdaQueryWrapper<BizItem>()
                        .ge(BizItem::getCreateTime, start)
                        .le(BizItem::getCreateTime, end)
                        .eq(BizItem::getDeleted, 0)
        );
        log.info("开始生成报表，statDate={}, start={}, end={}", statDate, start, end);
        // 2. 组装统计数据
        Map<String, Object> sourceData = new LinkedHashMap<>();
        sourceData.put("data", statDate.toString());
        sourceData.put("totalCount", items.size());
        sourceData.put("topLocations", topLocations(items));
        sourceData.put("topTypes", topTypes(items));
        sourceData.put("foundCount", countByType(items, BizItemTypeConstant.FOUND));
        sourceData.put("lostCount", countByType(items, BizItemTypeConstant.LOST));
        sourceData.put("closedCount", countByStatus(items, BizItemStatusConstant.CLOSED));
        log.info("组装统计数据完成，statDate={}, start={}, end={}", statDate, start, end);
        // 3. 调用AI生成总结
        String prompt = buildPrompt(sourceData);
        String summary = adminStatisticsAiClient.generateSummary(prompt);
        log.info("调用AI生成总结完成，statDate={}, start={}, end={}, summary={}", statDate, start, end, summary);
        // 4. 保存报表
        BizAiStatisticsReport report = new BizAiStatisticsReport();
        report.setStatDate(statDate);
        report.setStatType(statType);
        report.setSourceDataJson(toJson(sourceData));
        report.setAiSummary(summary);
        report.setModelName(aiProperties.getModel());
        log.info("保存报表，statDate={}, start={}, end={}", statDate, start, end);
        if (summary.equals(MessageConstant.AI_GENERATE_FAILED)) {
            report.setStatus("0");
        } else {
            report.setStatus("1");
        }
        log.info("保存报表完成，statDate={}, start={}, end={}", statDate, start, end);
        bizAiStatisticsReportDao.insert(report);
    }

    /**
     * 统计顶部位置（5个）
     * 1. 按位置分组
     * 2. 按数量排序
     * 3. 取前5个
     *
     * @param items
     * @return items
     * @return
     */
    private List<Map<String, Object>> topLocations(List<BizItem> items) {
        return items.stream()
                // 按位置分组，统计每个位置的物品数量
                .collect(Collectors.groupingBy(
                        item -> item.getLocation() == null || item.getLocation().isBlank()
                                ? "未知"
                                : item.getLocation(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                //2. 按数量排序
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                //3. 取前5个
                .limit(5)
                .map(e -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("location", e.getKey());
                    map.put("count", e.getValue());
                    return map;
                })
                .toList();
    }

    /**
     * 统计顶部物品类型（5个）
     *
     * @param items
     * @return items
     * 1. 按物品类型分组
     * 2. 按数量排序
     * 3. 取前5个
     * @return items
     */
    private List<Map<String, Object>> topTypes(List<BizItem> items) {
        return items.stream()
                //1. 按物品类型分组
                .collect(Collectors.groupingBy(
                        item -> item.getTitle() == null || item.getTitle().isBlank() ? "未知物品" : item.getTitle(),
                        Collectors.counting()
                ))
                .entrySet()
                //2. 按数量排序
                .stream()
                //3. 取前5个
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

    /**
     * 统计物品类型数量
     *
     * @param items 物品列表
     * @param type  物品类型
     * @return 物品数量
     */
    private long countByType(List<BizItem> items, String type) {
        return items.stream().filter(i -> type.equals(i.getType())).count();
    }

    /**
     * 统计物品状态数量
     *
     * @param items  物品列表
     * @param status 物品状态
     * @return 物品数量
     */
    private long countByStatus(List<BizItem> items, String status) {
        return items.stream().filter(i -> status.equals(i.getStatus())).count();
    }

    /**
     * 构建AI总结的提示
     *
     * @param sourceData 源数据
     * @return 提示
     */
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

    /**
     * 转换为JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("统计数据转JSON失败", e);
            return "{}";
        }
    }
}