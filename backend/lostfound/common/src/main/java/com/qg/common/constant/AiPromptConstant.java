package com.qg.common.constant;

import java.util.List;

public class AiPromptConstant {

    public static final String DEFAULT_IMAGE_CLASSIFICATION_TEMPLATE = """
    你是一个失物多模态智能助手，请根据以下信息生成物品分类、标签和描述：
    
    图片URL：%s
    物品名称：%s
    用户描述：%s
    丢失/拾取地点：%s

    要求：
    1. 输出JSON格式，包含以下字段：
       - aiCategory：物品分类，例如“手机”、“校卡”等。
       - aiTags：物品标签，包括颜色（如红色、蓝色）、材质（如皮革、塑料）、类型特征（如钥匙、钱包、手机）、场景特征（如学生、公园、教室）。
       - aiDescription：物品描述，例如“这是一个红色的手机，对学生很重要”。
    2. 禁止生成任何敏感信息。
""";


    private AiPromptConstant() {
    }
    public static final String DEFAULT_DESCRIPTION_TEMPLATE = "该物品为%s，请尽快联系失主。";
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final List<String> SENSITIVE_WORDS = List.of("身份证", "学号"); // 需要屏蔽的敏感信息

    public static final String ITEM_DESCRIPTION_PROMPT = """
    你是校园失物招领平台的智能辅助助手。
    请根据用户提供的物品信息生成一段简洁自然的描述，适合展示在失物招领平台上。

    输入信息：
    物品名称：%s
    用户原始描述：%s
    丢失/拾取地点：%s

    要求：
    1. 语言简洁自然，100字以内；
    2. 不要编造具体隐私信息；
    3. 突出物品用途、常见特征和找回提醒；
    4. 不要输出多余解释；
    5. 输出JSON格式，包含以下字段：
       - aiCategory：物品分类，例如“手机”、“校卡”等；
       - aiTags：物品标签，包括颜色（如红色、蓝色）、材质（如皮革、塑料）、类型特征（如钥匙、钱包、手机）、场景特征（如学生、公园、教室）；
       - aiDescription：物品描述，例如“这是一个红色的手机，对学生很重要”。
    6. 禁止生成敏感信息。
""";

}
