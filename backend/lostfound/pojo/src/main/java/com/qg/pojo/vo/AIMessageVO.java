package com.qg.pojo.vo;

import com.qg.pojo.entity.AiChatHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

@NoArgsConstructor
@Data
public class AIMessageVO {
    private String role;
    private String content;

    public AIMessageVO(Message message) {
        switch (message.getMessageType()) {
            case USER -> this.role = "user";
            case ASSISTANT -> this.role = "assistant";
            case SYSTEM -> this.role = "system";
        }
        this.content = message.getText();
    }

    public AIMessageVO(AiChatHistory aiChatHistory) {
    }
}
