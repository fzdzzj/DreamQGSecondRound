package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

import java.io.Serializable;

@NoArgsConstructor
@Data
@Schema(description = "消息VO")
public class MessageVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "角色")
    private String role;
    @Schema(description = "内容")
    private String content;

    public MessageVO(Message message) {
        switch (message.getMessageType()) {
            case USER -> this.role = "user";
            case ASSISTANT -> this.role = "assistant";
            case SYSTEM -> this.role = "system";
        }
        this.content = message.getText();
    }
}
