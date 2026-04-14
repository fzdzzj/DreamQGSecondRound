package com.qg.common.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 敏感词过滤工具类
 */
@Component
public class SensitiveWordFilterUtil {

    private final Map<String, Pattern> sensitivePatterns = new HashMap<>();

    public SensitiveWordFilterUtil() {
        // 1. 中文标识类 + 数字/字母 (保留关键词，屏蔽后面内容)
        addNumberAfterWord("电话");
        addNumberAfterWord("手机");
        addNumberAfterWord("手机号");
        addNumberAfterWord("电话号码");
        addNumberAfterWord("微信");
        addNumberAfterWord("微信号");
        addNumberAfterWord("微信码");
        addNumberAfterWord("QQ");
        addNumberAfterWord("QQ号");
        addNumberAfterWord("支付宝");
        addNumberAfterWord("银行卡");
        addNumberAfterWord("身份证");

        // 2. 校内标识类 + 数字 (保留关键词，屏蔽后面数字)
        addNumberAfterWord("学号");
        addNumberAfterWord("宿舍号");
        addNumberAfterWord("寝室号");

        // 3. 密码类：整词 + 后面内容全部屏蔽 → ***
        addFullMask("密码");
        addFullMask("password");
        addFullMask("支付密码");
    }

    /**
     * 保留关键词，只屏蔽后面内容
     */
    private void addNumberAfterWord(String word) {
        if (word == null || word.isBlank()) return;
        String regex = String.format("((?i)%s)[\\w\\d\\s]*", Pattern.quote(word));
        sensitivePatterns.put(word, Pattern.compile(regex));
    }

    /**
     * 密码专用：关键词 + 后面所有内容 全部变成 ***
     */
    private void addFullMask(String word) {
        if (word == null || word.isBlank()) return;
        String regex = String.format("(?i)%s[\\w\\d\\s]*", Pattern.quote(word));
        sensitivePatterns.put(word + "_full", Pattern.compile(regex));
    }

    /**
     * 过滤方法
     */
    public String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String result = text;
        for (Pattern pattern : sensitivePatterns.values()) {
            // 密码类没有分组，直接替换成 ***
            if (pattern.pattern().contains("_full")) {
                result = pattern.matcher(result).replaceAll("***");
            } else {
                result = pattern.matcher(result).replaceAll("$1***");
            }
        }
        return result;
    }
}