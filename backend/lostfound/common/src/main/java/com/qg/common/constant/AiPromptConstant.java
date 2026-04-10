package com.qg.common.constant;

import java.util.List;

public class AiPromptConstant {

    /**
     * 多模态物品描述提示词
     * 占位符顺序：
     * 1. 物品名称
     * 2. 用户原始描述
     * 3. 丢失地点
     */
    public static final String IMAGE_DESCRIPTION_PROMPT = """
                        "你是校园失物多模态智能助手，请根据以下信息生成分类和标签，并生成物品描述。\\n" +
                                "请严格返回 JSON 格式，字段必须包括：\\n" +
                                "  - aiCategory：物品分类，例如“手机”、“校卡”等；\\n" +
                                "  - aiTags：物品标签，包括颜色、材质、类型特征、场景特征；\\n" +
                                "  - aiDescription：物品描述，语言自然简洁，不超过100字。\\n" +
                                "禁止生成敏感信息，如身份证、学号等。\\n" +
                                "请确保输出是有效 JSON，且不要输出任何额外文字或解释。\\n" +
                                "物品名称：%s\\n" +
                                "用户描述：%s\\n" +
                                "丢失地点：%s\\n"
            """;




    private AiPromptConstant() {
    }
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final List<String> SENSITIVE_WORDS = List.of("身份证", "学号"); // 需要屏蔽的敏感信息

    public static final String DEFAULT_DESCRIPTION_TEMPLATE = """
你是校园失物招领平台的智能助手。
请根据用户提供的物品信息生成一段简洁自然的物品描述，适合展示在平台上。

输入信息：
物品名称：%s
用户描述：%s
丢失/拾取地点：%s

要求：
1. 输出JSON格式，包含以下字段：
   - aiCategory：物品分类，例如“手机”、“校卡”等；
   - aiTags：物品标签，包括颜色（如红色、蓝色）、材质（如皮革、塑料）、类型特征（如钥匙、钱包、手机）、场景特征（如学生、公园、教室）；
   - aiDescription：物品描述，例如“这是一个红色的手机，对学生很重要”；
2. aiDescription 字段语言自然简洁，不超过 100 字；
3. 突出物品用途、常见特征和找回提醒；
4. 禁止生成任何敏感信息；
5. 不要输出多余解释。
""";


}
