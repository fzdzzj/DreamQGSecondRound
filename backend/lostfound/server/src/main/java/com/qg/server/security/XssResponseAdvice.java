package com.qg.server.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局响应体处理类，用于防止 XSS 攻击。
 */
@ControllerAdvice
@Slf4j
public class XssResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    public XssResponseAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 排除 byte[] 类型，不拦截二进制数据
     */
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 排除返回值是 byte[] 的接口（文档、文件、图片都会跳过）
        return returnType.getParameterType() != byte[].class;
    }

    /**
     * 处理 JSON 响应，防止 XSS 攻击
     *
     * @param body
     * @param returnType
     * @param selectedContentType
     * @param selectedConverterType
     * @param request
     * @param response
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // 处理 byte[] 响应, 直接返回, 不处理
        if (body instanceof byte[]) {
            return body;
        }

        // 仅处理 JSON 响应
        if (selectedContentType.includes(MediaType.APPLICATION_JSON)) {
            try {
                JsonNode jsonNode = objectMapper.valueToTree(body);
                return escapeJsonNode(jsonNode);
            } catch (Exception e) {
                return body;
            }
        }
        return body;
    }
    /**
     * 递归处理 JSON 节点，防止 XSS 攻击
     * @param node
     * @return
     */
    private JsonNode escapeJsonNode(JsonNode node) {
        // 处理文本节点
        if (node.isTextual()) {
            String value = node.asText();
            if (value != null && !value.isEmpty()) {
                return objectMapper.valueToTree(StringEscapeUtils.escapeHtml4(value));
            }
        }
        // 处理对象节点
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fields().forEachRemaining(entry -> {
                objectNode.set(entry.getKey(), escapeJsonNode(entry.getValue()));
            });
            return objectNode;
        }
        // 处理数组节点
        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, escapeJsonNode(arrayNode.get(i)));
            }
            return arrayNode;
        }
        return node;
    }
}