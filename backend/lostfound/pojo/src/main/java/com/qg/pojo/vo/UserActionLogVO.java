package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "用户操作日志")
@Data
@NoArgsConstructor
public class UserActionLogVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "主键 ID")
    private Long id;
    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "操作类型 1：CLAIM / 2：EDIT_POST / 3.POST")

    private String actionType;

    @Schema(description = "操作类型描述")
    private String actionTypeDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
