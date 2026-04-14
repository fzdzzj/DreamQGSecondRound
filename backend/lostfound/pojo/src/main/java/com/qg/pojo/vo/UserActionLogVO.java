package com.qg.pojo.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
@Schema(description = "用户操作日志")
@Data
@NoArgsConstructor
public class UserActionLogVO implements Serializable {
    @Schema(description = "主键 ID")
    private Long id;
    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "操作类型 1：CLAIM / 2：EDIT_POST / 3.POST")
    /**
     * 1：CLAIM / 2：EDIT_POST / 3.POST
     */
    private String actionType;

    @Schema(description = "操作类型描述")
    private String actionTypeDesc;
    /**
     * 创建时间
     * 对应字段：create_time，默认当前时间戳
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
