package com.qg.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SensitiveWordFilterUtil {
    private final Map<String, Pattern>sensitivePatterns=new HashMap<>();

    public SensitiveWordFilterUtil(){
        addSensitiveWord("电话", true);
        addSensitiveWord("手机", true);
        addSensitiveWord("手机号", true);
        addSensitiveWord("电话号码", true);
        addSensitiveWord("微信", true);
        addSensitiveWord("微信号", true);
        addSensitiveWord("微信码", true);
        addSensitiveWord("qq", true);
        addSensitiveWord("QQ号", true);
        addSensitiveWord("支付宝", true);
        addSensitiveWord("银行卡", true);
        addSensitiveWord("身份证", true);
        addSensitiveWord("学号", true);
        addSensitiveWord("宿舍号", true);
        addSensitiveWord("寝室号", true);
        addSensitiveWord("密码", true);
        addSensitiveWord("password", true);
        addSensitiveWord("支付密码", true);
    }

    public void addSensitiveWord(String word,boolean fuzzy){
        if(word==null||word.isBlank())return;
        String patternStr;
        if(fuzzy){
            patternStr="(?i)"+Pattern.quote(word)+"[\\w\\d\\\\u4e00-\\\\u9fa5]*";
        }else{
            patternStr="(?i)"+Pattern.quote(word);
        }
        sensitivePatterns.put(word,Pattern.compile(patternStr));
    }

    public String filter(String text){
        if(text==null||text.isEmpty())return text;
        String filtered=text;
        for(Pattern pattern:sensitivePatterns.values()){
            filtered=pattern.matcher(filtered).replaceAll("***");
        }
        return filtered;
    }
}
