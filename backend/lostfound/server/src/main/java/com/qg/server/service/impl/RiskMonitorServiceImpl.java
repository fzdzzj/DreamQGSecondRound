package com.qg.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 风险监控服务实现类
 * 1. 高价值物品丢失检测
 * 2. 敏感物品丢失检测
 * 3. 时段聚集性物品丢失检测
 * 4. 周期聚集性物品丢失检测
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiskMonitorServiceImpl implements RiskMonitorService {
    private final BizRiskEventDao bizRiskEventDao;
    private final WeComNotifyService weComNotifyService;
    private final BizItemDao bizItemDao;
    private final ApplicationContext applicationContext;
    private RiskMonitorServiceImpl getSelf() {
        return applicationContext.getBean(RiskMonitorServiceImpl.class);
    }

    /**
     * 物品发布事件
     * @param item 物品信息
     *              1. 高价值物品丢失检测
     *              2. 敏感物品丢失检测
     *              3. 时段聚集性物品丢失检测
     */
    @Override
    public void onItemPublished(BizItem item) {
        detectHighValueRisk(item);
        detectSensitiveItemRisk(item);
        detectLocationClusterRisk(item);
    }

    /**
     * 周期聚集性物品丢失检测
     *
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

    /**
     * 物品找到事件
     * @param item
     *              1. 判断是否为高价值物品丢失
     *              2. 判断是否为敏感物品丢失
     *              3. 判断是否为时段聚集性物品丢失
     *              4. 更新风险事件状态为已解决
     *              5. 发布物品找回风险事件
     */
    @Override
    public void onItemFound(BizItem item) {
        if(!isHighValueItem(item)&&!isSensitiveItem(item)&&!(getLocationClusterCount(item)>0)){
            log.info("物品状态正常修改");
            return;
        }
        log.info("物品已经找回");
        //4. 更新风险事件状态为已解决
        List<BizRiskEvent> foundEventList = bizRiskEventDao.selectList(new LambdaQueryWrapper<BizRiskEvent>()
                .eq(BizRiskEvent::getRelatedItemId, item.getId()));
        if(foundEventList!=null&&!foundEventList.isEmpty()){
            for(BizRiskEvent foundEvent:foundEventList){
                BizRiskEvent event = new BizRiskEvent();
                event.setId(foundEvent.getId());
                event.setHandleStatus(RiskConstant.HANDLE_STATUS_RESOLVED);
                event.setHandleRemark("物品已经找回");
                event.setHandledTime(LocalDateTime.now());
                event.setNotifyStatus(RiskConstant.NOTIFY_STATUS_SUCCESS);
                event.setRiskType(RiskConstant.RISK_TYPE_ITEM_FOUND);
                event.setRiskLevel(RiskConstant.RISK_LEVEL_NO);
                bizRiskEventDao.updateById(event);
            }
        }
        //5. 发布物品找回风险事件
        log.info("物品已经找回，开始发布物品找回风险事件");
        getSelf().createRiskEvent(RiskConstant.RISK_TYPE_ITEM_FOUND,
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
     *
     * 1. 检测物品描述中是否包含高价值物品关键词
     * 2. 发布高价值物品丢失风险事件
     */
    private void detectHighValueRisk(BizItem item) {
        if (!isHighValueItem(item)) {
            return;
        }
        log.info("物品描述中包含高价值物品关键词，开始发布风险事件通知");
        getSelf().createRiskEvent(
                RiskConstant.RISK_TYPE_HIGH_VALUE_ITEM,
                RiskConstant.RISK_LEVEL_HIGH,
                "疑似贵重物品丢失",
                "检测到贵重物品"+item.getTitle()+"，描述为："+item.getDescription()+"丢失，建议管理员关注冒领风险",
                item.getId(),
                item.getUserId(),
                item.getLocation(),
                "{\"rule\":\"HIGH_VALUE_ITEM\"}"
        );
    }

    /**
     * 高价值物品丢失检测
     *
     * @param item 物品
     * @return 是否为高价值物品丢失
     * 检测物品描述中是否包含高价值物品关键词
     */
    private boolean isHighValueItem(BizItem item) {
        log.info("开始检测高价值物品丢失");
        String text = buildText(item);
        if (!containsAny(text, "单车", "自行车", "iphone", "苹果手机", "电脑", "笔记本", "相机", "平板", "手机")) {
            log.info("物品描述中未包含高价值物品关键词，无需检测");
            return false;
        }
        log.info("物品描述中包含高价值物品关键词，开始发布风险事件通知");
        return true;
    }

    /**
     * 地点聚集性物品丢失检测
     *
     * 1. 检测物品描述中是否包含地点聚集性关键词
     * 2. 发布地点聚集性物品丢失风险事件
     * @param item 物品
     */
    private void detectLocationClusterRisk(BizItem item) {
        if(getLocationClusterCount(item)==0L){
            return;
        }
        Long count = getLocationClusterCount(item);
        log.info("物品描述中包含地点聚集性关键词，开始发布风险事件通知");
        getSelf().createRiskEvent(
                RiskConstant.RISK_TYPE_PERIODIC_CLUSTER,
                RiskConstant.RISK_LEVEL_MEDIUM,
                "疑似地点聚集性丢失",
                "近30分钟内同地点多次出现失物信息"+item.getTitle()+"，描述为："+item.getDescription()+"，建议管理员排查是否存在聚集性失窃",
                item.getId(),
                item.getUserId(),
                item.getLocation(),
                "{\"rule\":\"LOCATION_CLUSTER\",\"count\":" + count + "}"
        );
    }

    /**
     * 地点聚集性物品丢失检测
     *
     * @param item 物品
     * @return 地点聚集性物品丢失次数
     * 1. 判断物品描述中是否包含地点信息
     * 2. 获取7天内相同地点失物信息数量，判断是否超过3次
     */
    private long getLocationClusterCount(BizItem item) {
        log.info("开始检测地点聚集性");
        // 1. 判断物品描述中是否包含地点信息
        if (item.getLocation() == null || item.getLocation().isBlank()) {
            log.info("物品描述中未包含地点信息，无需检测");
            return 0L;
        }
        log.info("物品描述中包含地点信息，开始检测地点聚集性");
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(7);
        // 2. 获取7天内相同地点失物信息数量，判断是否超过3次
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

    /**
     * 敏感证件类物品丢失检测
     *
     * 1. 检测物品描述中是否包含敏感证件类物品关键词
     * 2. 发布敏感证件类物品丢失风险事件
     * @param item 物品
     */
    private void detectSensitiveItemRisk(BizItem item) {
        if(!isSensitiveItem(item)){
            return;
        }
        log.info("物品描述中包含敏感证件类物品关键词，开始发布风险事件通知");
        getSelf().createRiskEvent(
                RiskConstant.RISK_TYPE_SENSITIVE_ITEM,
                RiskConstant.RISK_LEVEL_HIGH,
                "疑似敏感证件类物品丢失",
                "检测到证件类敏感物品"+item.getTitle()+"，描述为："+item.getDescription()+"丢失，建议管理员关注冒领风险",
                item.getId(),
                item.getUserId(),
                item.getLocation(),
                "{\"rule\":\"SENSITIVE_ITEM\"}"
        );
    }

    /**
     * 敏感证件类物品丢失检测
     *
     * @param item 物品
     * @return 是否为敏感证件类物品丢失
     * 1. 检测物品描述中是否包含敏感证件类物品关键词
     */
    private boolean isSensitiveItem(BizItem item) {
        log.info("开始检测敏感物品丢失");
        String text = buildText(item);
        if (!containsAny(text, "校园卡", "身份证", "学生证", "护照", "银行卡")) {
            log.info("物品描述中未包含敏感物品关键词，无需检测");
            return false;
        }
        log.info("物品描述中包含敏感物品关键词，开始发布风险事件通知");
        return true;
    }

    /**
     * 创建风险事件
     *
     * @param riskType 风险类型
     * @param riskLevel 风险等级
     * @param title 风险标题
     * @param content 风险内容
     * @param itemId 关联物品ID
     * @param userId 关联用户ID
     * @param location 风险发生地点
     * @param evidenceJson 风险证据JSON
     */
    @Transactional(rollbackFor = Exception.class)
    public void createRiskEvent(String riskType,
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

    /**
     * 构建物品描述文本
     *
     * @param item 物品
     * @return 物品描述文本
     */
    private String buildText(BizItem item) {
        String title = item.getTitle() == null ? "" : item.getTitle();
        String description = item.getDescription() == null ? "" : item.getDescription();
        return (Objects.toString(title, "") + " " + Objects.toString(description, "")).trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 检查文本是否包含任意关键词
     *
     * @param text 文本
     * @param words 关键词数组
     * @return 是否包含任意关键词
     */
    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}