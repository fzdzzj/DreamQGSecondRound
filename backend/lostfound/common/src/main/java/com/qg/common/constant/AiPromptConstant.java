package com.qg.common.constant;

import java.util.List;

public class AiPromptConstant {

    /**
     * 多模态物品描述提示词
     * 占位符顺序：
     * 1. 物品名称
     * 2. 用户原始描述
     * 3. 丢失地点
     * 4. 图片类型
     */
    public static final String IMAGE_DESCRIPTION_PROMPT =
            "你是校园失物多模态智能助手，请根据以下信息生成分类和标签，并生成物品描述。\n" +
                    "请严格返回 JSON 格式，字段必须包括：\n" +
                    "  - aiCategory\n" +
                    "  - aiTags\n" +
                    "  - aiDescription\n" +
                    "禁止生成敏感信息，如身份证、学号等。\n" +
                    "请确保输出是有效 JSON，且不要输出任何额外文字或解释。\n" +
                    "物品名称：%s\n" +
                    "用户描述：%s\n" +
                    "丢失地点：%s\n" +
                    "类型：%s";





    private AiPromptConstant() {
    }
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final List<String> SENSITIVE_WORDS = List.of("身份证", "学号"); // 需要屏蔽的敏感信息
    public static final String DDEFAULT_DESCRIPTION="""
物品名称：%s
物品描述：%s
丢失地点：%s
""";
    public static final String DEFAULT_DESCRIPTION_TEMPLATE = """
            你是校园失物多模态智能助手，请根据以下信息生成分类和标签，并生成物品描述。
                        请严格返回 JSON 格式，字段必须包括：
                          - aiCategory
                          - aiTags
                          - aiDescription
                        禁止生成敏感信息，如身份证、学号等。
                        请确保输出是有效 JSON，不要输出额外文字。
                        物品名称：%s （必须严格使用，不要更改）
                        用户描述：%s
                        丢失地点：%s
                        类型：%s
                        规则：
                        1. aiCategory 必须与物品名称对应；
                        2. aiDescription 可以解释用户描述与图片内容冲突；
                        3. aiTags 描述物品颜色、材质、特征、场景等；
            
""";


}
