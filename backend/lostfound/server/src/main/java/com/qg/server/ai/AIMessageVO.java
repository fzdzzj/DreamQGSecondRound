package com.qg.server.ai;

import com.qg.pojo.entity.AiChatHistory;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AIMessageVO {
    private String role;
    private String content;



    public AIMessageVO(AiChatHistory aiChatHistory) {
    }
}
