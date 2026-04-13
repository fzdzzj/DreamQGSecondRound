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
 *
 * 该类会在 Spring MVC 控制器返回 JSON 响应前，对响应体中的所有字符串字段进行 HTML 转义，
 * 避免恶意脚本注入到前端页面。
 */
@ControllerAdvice
public class XssResponseAdvice implements ResponseBodyAdvice<Object> {

    /**
     * Jackson 的对象映射器，用于将对象转换为 JsonNode，方便递归处理。
     */
    private final ObjectMapper objectMapper;

    /**
     * 构造方法，注入 ObjectMapper
     *
     * @param objectMapper Jackson 对象映射器
     */
    public XssResponseAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 判断该响应是否需要被处理。
     *
     * @param returnType      控制器方法返回类型
     * @param converterType   响应体转换器类型
     * @return true 表示对所有响应体都进行处理
     */
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 返回 true 表示拦截所有控制器返回的响应体
        return true;
    }

    /**
     * 在响应体写入前进行处理。
     *
     * @param body                   控制器返回的响应体对象
     * @param returnType             控制器方法返回类型
     * @param selectedContentType    当前请求的 Content-Type
     * @param selectedConverterType  响应体转换器类型
     * @param request                HTTP 请求对象
     * @param response               HTTP 响应对象
     * @return 经过 XSS 转义后的响应体
     */
    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        // 仅处理 Content-Type 为 application/json 的响应
        if (selectedContentType.includes(MediaType.APPLICATION_JSON)) {
            try {
                // 将响应对象转换为 JsonNode
                JsonNode jsonNode = objectMapper.valueToTree(body);
                // 对 JsonNode 中的字符串进行 HTML 转义
                return escapeJsonNode(jsonNode);
            } catch (Exception e) {
                // 如果处理失败，返回原始响应体
                return body;
            }
        }
        // 非 JSON 类型的响应，直接返回
        return body;
    }

    /**
     * 对 JsonNode 节点进行递归转义。
     *
     * 支持三种节点类型：
     * 1. 字符串节点 → 使用 StringEscapeUtils.escapeHtml4 转义
     * 2. 对象节点 → 遍历字段递归调用 escapeJsonNode
     * 3. 数组节点 → 遍历元素递归调用 escapeJsonNode
     *
     * @param node 待转义的 JsonNode
     * @return 转义后的 JsonNode
     */
    private JsonNode escapeJsonNode(JsonNode node) {
        // 字符串节点 → HTML 转义
        if (node.isTextual()) {
            String value = node.asText();
            if (value != null && !value.isEmpty()) {
                // 转义 HTML 特殊字符，例如 < > & "
                return objectMapper.valueToTree(StringEscapeUtils.escapeHtml4(value));
            }
        }

        // 对象节点 → 遍历每个字段递归处理
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fields().forEachRemaining(entry -> {
                objectNode.set(entry.getKey(), escapeJsonNode(entry.getValue()));
            });
            return objectNode;
        }

        // 数组节点 → 遍历每个元素递归处理
        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                arrayNode.set(i, escapeJsonNode(arrayNode.get(i)));
            }
            return arrayNode;
        }

        // 其他类型（数字、布尔、null）直接返回
        return node;
    }
}
