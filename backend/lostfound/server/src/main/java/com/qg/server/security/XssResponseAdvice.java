package com.qg.server.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
public class XssResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    public XssResponseAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 修复点1：排除 byte[] 类型，不拦截二进制数据
     */
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 排除返回值是 byte[] 的接口（文档、文件、图片都会跳过）
        if (returnType.getParameterType() == byte[].class) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        // 修复点2：如果是二进制/文件类型，直接返回，不处理
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

    private JsonNode escapeJsonNode(JsonNode node) {
        if (node.isTextual()) {
            String value = node.asText();
            if (value != null && !value.isEmpty()) {
                return objectMapper.valueToTree(StringEscapeUtils.escapeHtml4(value));
            }
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fields().forEachRemaining(entry -> {
                objectNode.set(entry.getKey(), escapeJsonNode(entry.getValue()));
            });
            return objectNode;
        }
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