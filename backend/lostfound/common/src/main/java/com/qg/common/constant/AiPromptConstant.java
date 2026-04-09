package com.qg.common.constant;

public class AiPromptConstant {

    private AiPromptConstant() {
    }

    public static final String ITEM_DESCRIPTION_PROMPT = """
            你是校园失物招领平台的辅助助手。
            请根据用户提供的物品信息，生成一段简洁、自然、适合展示在失物招领平台上的物品描述。
            要求：
            1. 不要编造过于具体的隐私信息；
            2. 语言简洁自然，100字以内；
            3. 突出物品用途、常见特征和找回提醒；
            4. 不要输出多余解释。

            物品名称：%s
            用户原始描述：%s
            地点：%s
            """;
}
