package com.qg.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "聊天记录游标查询DTO")
public class PrivateMessageHistoryQueryDTO {

    @Schema(description = "对方用户ID")
    private Long peerId;

    @Schema(description = "上一页最早一条消息ID，首次查询可不传")
    private Long lastMessageId;

    @Schema(description = "每页大小")
    private Integer pageSize = 20;
}
