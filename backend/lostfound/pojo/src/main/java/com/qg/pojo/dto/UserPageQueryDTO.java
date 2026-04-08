package com.qg.pojo.dto;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(name = "用户分页查询参数")
public class UserPageQueryDTO {
    @Min(value = 1L, message = "页码不能小于1")
    private Integer pageNum;
    @Max(value = 100, message = "页大小不能大于100")
    @Min(value = 1, message = "页大小不能小于1")
    private Integer pageSize;
    @Schema(description = "用户id")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "角色")
    private String role;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "最后登录时间")
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
