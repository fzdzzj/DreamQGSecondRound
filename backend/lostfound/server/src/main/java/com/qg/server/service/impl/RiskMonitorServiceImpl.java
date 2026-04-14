package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qg.common.constant.BizItemTypeConstant;
import com.qg.common.constant.RiskConstant;
import com.qg.pojo.entity.BizItem;
import com.qg.pojo.entity.BizRiskEvent;
import com.qg.server.mapper.BizItemDao;
import com.qg.server.mapper.BizRiskEventDao;
import com.qg.server.service.RiskMonitorService;
import com.qg.server.service.WeComNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskMonitorServiceImpl implements RiskMonitorService {
    private final BizRiskEventDao bizRiskEventDao;
    private final WeComNotifyService weComNotifyService;
    private final BizItemDao bizItemDao;
    @Override
    public void onItemPublished(BizItem item) {
        detectHighValueRisk(item);
        detectSensitiveItemRisk(item);
        detectLocationClusterRisk(item);
    }
    /**
     * 周期聚集性物品丢失检测
     */
    public void scanPeriodicClusterRisk() {
        log.info(" 开始扫描时段聚集风险");

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMinutes(30);
        log.info("扫描时间范围：{} -> {}", start, end);

        // 1. 按地点 + 时段分组统计
        List<BizItem> itemList = bizItemDao.selectList(new LambdaQueryWrapper<BizItem>()
                .eq(BizItem::getType, BizItemTypeConstant.LOST)
                .between(BizItem::getCreateTime, start, end)
        );

        // key: location + timeWindow
        Map<String, List<BizItem>> groupMap = new HashMap<>();

        for (BizItem item : itemList) {
            String location = item.getLocation();
            String timeWindow = getTimeWindow(item.getCreateTime().toLocalTime());
            String key = location + " | " + timeWindow;

            groupMap.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }

        // 2. 同一地点+同一时段 >3条 → 生成风险
        for (Map.Entry<String, List<BizItem>> entry : groupMap.entrySet()) {
            List<BizItem> list = entry.getValue();
            if (list.size() >= 3) {

                BizItem item = list.get(0);
                String location = item.getLocation();
                String timeWindow = getTimeWindow(item.getCreateTime().toLocalTime());

                log.warn("【风险触发】地点：{}，时段：{}，近7天丢失数量：{}",
                        location, timeWindow, list.size());

                BizRiskEvent event = new BizRiskEvent();
                event.setRiskType(RiskConstant.RISK_TYPE_PERIODIC_CLUSTER);
                event.setRiskLevel(RiskConstant.RISK_LEVEL_HIGH);
                event.setTitle("周期聚集性物品丢失风险");
                event.setContent("地点：" + location + "，时段：" + timeWindow + "，近7天丢失数量：" + list.size());
                event.setRelatedItemId(item.getId());
                event.setRelatedUserId(item.getUserId());
                event.setLocation(location);
                event.setEvidenceJson("{\"rule\":\"PERIODIC_CLUSTER\"}");
                event.setNotifyStatus(RiskConstant.NOTIFY_STATUS_PENDING);
                event.setHandleStatus(RiskConstant.HANDLE_STATUS_UNHANDLED);
                log.info("风险事件创建成功");
                bizRiskEventDao.insert(event);
            }
        }

        log.info("时段聚集风险扫描完成");
    }

    @Override
    public void onItemFound(BizItem item) {
        if(!judgeHighValueItemRisk(item)&&!judgeSensitiveItemRisk(item)&&!(judgeLocationClusterRisk(item)>0)){
            log.info("物品状态正常修改");
            return;
        }
        log.info("物品已经找回");
        BizRiskEvent event = new BizRiskEvent();
        BizRiskEvent foundEvent = bizRiskEventDao.selectOne(new LambdaQueryWrapper<BizRiskEvent>()
                .eq(BizRiskEvent::getRelatedItemId, item.getId()));
        if(foundEvent!=null){
            event.setId(foundEvent.getId());
            event.setHandleStatus(RiskConstant.HANDLE_STATUS_RESOLVED);
            event.setHandleRemark("物品已经找回");
            event.setHandledTime(LocalDateTime.now());
            event.setNotifyStatus(RiskConstant.NOTIFY_STATUS_SUCCESS);
            event.setRiskType(RiskConstant.RISK_TYPE_ITEM_FOUND);
            event.setRiskLevel(RiskConstant.RISK_LEVEL_NO);
            bizRiskEventDao.updateById(event);
        }
        createRiskEvent(RiskConstant.RISK_TYPE_ITEM_FOUND,
                RiskConstant.RISK_LEVEL_NO, "物品已经找回", "物品已经找回",
                item.getId(),
                item.getUserId(),
                item.getLocation(),
                "{\"rule\":\"ITEM_FOUND\"}");
    }

    /**
     * 生成时段
     */
    private String getTimeWindow(LocalTime time) {
        int hour = time.getHour();
        return hour + ":00~" + (hour + 1) + ":00";
    }
    /**
     * 高价值物品丢失检测
     * @param item
     */
    private void detectHighValueRisk(BizItem item) {
        if (!judgeHighValueItemRisk(item)) {
            return;
        }
        log.info("物品描述中包含高价值物品关键词，开始发布风险事件通知");
        createRiskEvent(
                RiskConstant.RISK_TYPE_SENSITIVE_ITEM,
                RiskConstant.RISK_LEVEL_HIGH,
                "疑似敏感证件类物品丢失",
                "检测到证件类敏感物品丢失，建议管理员关注冒领风险",
                item.getId(),
                item.getUserId(),
                item.getLocation(),
                "{\"rule\":\"SENSITIVE_ITEM\"}"
        );
    }
    private boolean judgeHighValueItemRisk(BizItem item) {
        log.info("开始检测高价值物品丢失");
        String text = buildText(item);
        if (!containsAny(text, "单车", "自行车", "iphone", "苹果手机", "电脑", "笔记本", "相机", "平板", "手机")) {
            log.info("物品描述中未包含高价值物品关键词，无需检测");
            return false;
        }
        log.info("物品描述中包含高价值物品关键词，开始发布风险事件通知");
        return true;
    }

    private void detectLocationClusterRisk(BizItem item) {
        if(judgeLocationClusterRisk(item)==0L){
            return;
        }
        Long count = judgeLocationClusterRisk(item);
        log.info("物品描述中包含地点聚集性关键词，开始发布风险事件通知");
        createRiskEvent(
                RiskConstant.RISK_TYPE_PERIODIC_CLUSTER,
                RiskConstant.RISK_LEVEL_MEDIUM,
                "疑似地点聚集性丢失",
                "近30分钟内同地点多次出现失物信息，建议管理员排查是否存在聚集性失窃",
                item.getId(),
                item.getUserId(),
                item.getLocation(),
                "{\"rule\":\"LOCATION_CLUSTER\",\"count\":" + count + "}"
        );
    }
    private Long judgeLocationClusterRisk(BizItem item) {
        log.info("开始检测地点聚集性");
        if (item.getLocation() == null || item.getLocation().isBlank()) {
            log.info("物品描述中未包含地点信息，无需检测");
            return 0L;
        }
        log.info("物品描述中包含地点信息，开始检测地点聚集性");
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(7);

        Long count = bizItemDao.selectCount(new LambdaQueryWrapper<>(BizItem.class)
                .like(BizItem::getLocation, item.getLocation())
                .between(BizItem::getCreateTime, start, end));

        if (count == null || count < 3) {
            log.info("物品描述中未包含地点聚集性关键词，无需检测");
            return 0L;
        }
        log.info("物品描述中包含地点聚集性关键词，开始发布风险事件通知");
        return count;
    }
    private void detectSensitiveItemRisk(BizItem item) {
        if(!judgeSensitiveItemRisk(item)){
            return;
        }
        createRiskEvent(
                RiskConstant.RISK_TYPE_SENSITIVE_ITEM,
                RiskConstant.RISK_LEVEL_HIGH,
                "疑似敏感证件类物品丢失",
                "检测到证件类敏感物品丢失，建议管理员关注冒领风险",
                item.getId(),
                item.getUserId(),
                item.getLocation(),
                "{\"rule\":\"SENSITIVE_ITEM\"}"
        );
    }
    private boolean judgeSensitiveItemRisk(BizItem item) {
        log.info("开始检测敏感物品丢失");
        String text = buildText(item);
        if (!containsAny(text, "校园卡", "身份证", "学生证", "护照", "银行卡")) {
            log.info("物品描述中未包含敏感物品关键词，无需检测");
            return false;
        }
        log.info("物品描述中包含敏感物品关键词，开始发布风险事件通知");
        return true;
    }
    private void createRiskEvent(String riskType,
                                 String riskLevel,
                                 String title,
                                 String content,
                                 Long itemId,
                                 Long userId,
                                 String location,
                                 String evidenceJson) {
        log.info("开始创建风险事件");
        BizRiskEvent event = new BizRiskEvent();
        event.setRiskType(riskType);
        event.setRiskLevel(riskLevel);
        event.setTitle(title);
        event.setContent(content);
        event.setRelatedItemId(itemId);
        event.setRelatedUserId(userId);
        event.setLocation(location);
        event.setEvidenceJson(evidenceJson);
        event.setNotifyStatus(RiskConstant.NOTIFY_STATUS_PENDING);
        event.setHandleStatus(RiskConstant.HANDLE_STATUS_UNHANDLED);
        log.info("风险事件创建成功");
        bizRiskEventDao.insert(event);
        log.info("风险事件已保存到数据库");

        try {
            log.info("开始发送企业微信群机器人告警");
            weComNotifyService.notifyRiskEvent(event);
            log.info("企业微信群机器人告警成功");
            event.setNotifyStatus(RiskConstant.NOTIFY_STATUS_SUCCESS);
        } catch (Exception e) {
            log.error("企业微信群机器人告警失败", e);
            event.setNotifyStatus(RiskConstant.NOTIFY_STATUS_FAIL);
        }
        log.info("风险事件状态更新成功");
        bizRiskEventDao.updateById(event);
        log.info("风险事件状态已保存到数据库");
    }

    private String buildText(BizItem item) {
        String title = item.getTitle() == null ? "" : item.getTitle();
        String description = item.getDescription() == null ? "" : item.getDescription();
        return (title + " " + description).toLowerCase(Locale.ROOT);
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
