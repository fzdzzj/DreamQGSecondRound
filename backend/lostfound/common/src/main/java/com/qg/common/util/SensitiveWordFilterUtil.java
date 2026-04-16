package com.qg.common.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 敏感信息过滤工具类
 */
@Component
public class SensitiveWordFilterUtil {

    private final List<Rule> rules = new ArrayList<>();

    public SensitiveWordFilterUtil() {

        // 1. 保留关键词，只屏蔽后面的值
        addKeepKeywordMaskValue("电话");
        addKeepKeywordMaskValue("手机号");
        addKeepKeywordMaskValue("手机");
        addKeepKeywordMaskValue("电话号码");
        addKeepKeywordMaskValue("联系电话");
        addKeepKeywordMaskValue("联系方式");
        addKeepKeywordMaskValue("联系号码");

        addKeepKeywordMaskValue("微信");
        addKeepKeywordMaskValue("微信号");
        addKeepKeywordMaskValue("微信码");
        addKeepKeywordMaskValue("vx");
        addKeepKeywordMaskValue("vx号");
        addKeepKeywordMaskValue("wechat");

        addKeepKeywordMaskValue("QQ");
        addKeepKeywordMaskValue("QQ号");
        addKeepKeywordMaskValue("qq");
        addKeepKeywordMaskValue("qq号");
        addKeepKeywordMaskValue("扣扣");

        addKeepKeywordMaskValue("支付宝");
        addKeepKeywordMaskValue("支付宝号");
        addKeepKeywordMaskValue("zfb");

        addKeepKeywordMaskValue("银行卡");
        addKeepKeywordMaskValue("银行卡号");
        addKeepKeywordMaskValue("卡号");

        addKeepKeywordMaskValue("身份证");
        addKeepKeywordMaskValue("身份证号");

        addKeepKeywordMaskValue("学号");
        addKeepKeywordMaskValue("工号");
        addKeepKeywordMaskValue("宿舍号");
        addKeepKeywordMaskValue("寝室号");
        addKeepKeywordMaskValue("门牌号");
        addKeepKeywordMaskValue("车牌号");

        addKeepKeywordMaskValue("邮箱");
        addKeepKeywordMaskValue("邮箱号");
        addKeepKeywordMaskValue("email");
        addKeepKeywordMaskValue("e-mail");

        addKeepKeywordMaskValue("网址");
        addKeepKeywordMaskValue("链接");
        addKeepKeywordMaskValue("二维码");
        addKeepKeywordMaskValue("收款码");

        // 2. 整段直接替换成 ***
        addFullMask("密码");
        addFullMask("登录密码");
        addFullMask("支付密码");
        addFullMask("提现密码");
        addFullMask("银行卡密码");
        addFullMask("password");
        addFullMask("pwd");
        addFullMask("pass");

        // 3. 直接匹配常见敏感值
        addDirectMask("\\b1[3-9]\\d{9}\\b"); // 手机号
        addDirectMask("\\b[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}\\b"); // 邮箱
        addDirectMask("\\b[1-9]\\d{4,10}\\b"); // QQ号（粗略）
        addDirectMask("\\b\\d{17}[\\dXx]\\b"); // 身份证
        addDirectMask("\\b\\d{16,19}\\b"); // 银行卡
        //(?:...) 只是把它们包成一组，不做额外操作
        addDirectMask("\\b(?:https?://|www\\.)[^\\s]+\\b"); // URL

        // 4. 带分隔的手机号/QQ等简单变体
        addDirectMask("1[3-9]\\d[-_\\s]?\\d{4}[-_\\s]?\\d{4}"); // 138-0013-8000 / 138 0013 8000
        addDirectMask("[1-9]\\d{4,10}"); // QQ 类数字，会有误伤风险
    }

    /**
     * 保留关键词，只屏蔽后面的值
     * 支持：
     * 电话123456
     * 电话:123456
     * 电话：123456
     * 电话是123456
     * 电话 = 123456
     */
    private void addKeepKeywordMaskValue(String word) {
        if (word == null || word.isBlank()) return;

        String regex = String.format(
                "(?i)(%s)\\s*[:：是为=\\-]?\\s*([A-Za-z0-9_@.\\-/]{2,})",
                Pattern.quote(word)
        );
        rules.add(new Rule(Pattern.compile(regex), "$1***"));
    }

    /**
     * 整段替换
     * 例如：
     * 密码123456
     * 密码: 123456
     * password=abc123
     */
    private void addFullMask(String word) {
        if (word == null || word.isBlank()) return;

        String regex = String.format(
                "(?i)%s\\s*[:：是为=\\-]?\\s*([A-Za-z0-9_@.\\-!#$%%^&*]{1,})",
                Pattern.quote(word)
        );
        rules.add(new Rule(Pattern.compile(regex), "***"));
    }

    /**
     * 完整短语直接替换
     */
    private void addFullTextMask(String phrase) {
        if (phrase == null || phrase.isBlank()) return;
        rules.add(new Rule(Pattern.compile("(?i)" + Pattern.quote(phrase)), "***"));
    }

    /**
     * 直接匹配敏感值
     */
    private void addDirectMask(String regex) {
        rules.add(new Rule(Pattern.compile(regex, Pattern.CASE_INSENSITIVE), "***"));
    }

    /**
     * 过滤方法
     */
    public String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = normalize(text);

        for (Rule rule : rules) {
            result = rule.pattern.matcher(result).replaceAll(rule.replacement);
        }
        return result;
    }

    /**
     * 归一化，处理部分全角符号和大小写问题
     */
    private String normalize(String text) {
        if (text == null) return null;
        return text
                .replace('：', ':')
                .replace('（', '(')
                .replace('）', ')')
                .replace('－', '-')
                .replace('　', ' ')
                .trim();
    }

    private static class Rule {
        private final Pattern pattern;
        private final String replacement;

        public Rule(Pattern pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }
    }
}
