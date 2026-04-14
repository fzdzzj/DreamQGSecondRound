package com.qg.server.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qg.pojo.entity.BizRiskEvent;
import com.qg.server.risk.config.WeComRobotProperties;
import com.qg.server.service.WeComNotifyService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeComNotifyServiceImpl implements WeComNotifyService {

    private final WeComRobotProperties properties;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public void notifyRiskEvent(BizRiskEvent event) {
        if (Boolean.FALSE.equals(properties.getEnabled()) || properties.getWebhook() == null || properties.getWebhook().isBlank()) {
            log.warn("企业微信群机器人未启用或 webhook 未配置");
            return;
        }

        String content = """
                【校园AI失物招领平台风险告警】
                风险类型：%s
                风险等级：%s
                风险标题：%s
                涉及地点：%s
                事件内容：%s
                物品ID：%s
                用户ID：%s
                发生时间：%s
                """
                .formatted(
                        event.getRiskType(),
                        event.getRiskLevel(),
                        event.getTitle(),
                        event.getLocation() == null ? "-" : event.getLocation(),
                        event.getContent(),
                        event.getRelatedItemId() == null ? "-" : event.getRelatedItemId(),
                        event.getRelatedUserId() == null ? "-" : event.getRelatedUserId(),
                        event.getCreatedTime()
                );

        WeComTextMessage req = new WeComTextMessage();
        req.setMsgType("text");
        WeComTextMessage.Text text = new WeComTextMessage.Text();
        text.setContent(content);
        req.setText(text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.postForObject(properties.getWebhook(), new HttpEntity<>(req, headers), String.class);
    }

    @Data
    public static class WeComTextMessage {
        @JsonProperty("msgtype")
        private String msgType;
        private Text text;

        @Data
        public static class Text {
            private String content;
        }
    }
}
