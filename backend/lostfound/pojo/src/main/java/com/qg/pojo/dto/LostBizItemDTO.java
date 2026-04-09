package com.qg.pojo.dto;

import com.baomidou.mybatisplus.annotation.*;
import com.qg.common.constant.BizItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "丢失物品信息DTO")
@Data
public class LostBizItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 标题/物品名称
     */
    @Schema(description = "标题/物品名称", required = true, example = "手机")
    @NotBlank(message = "标题/物品名称不能为空")
    private String title;

    /**
     * 详细描述
     */
    @Schema(description = "详细描述", required = true, example = "手机被偷了")
    @NotBlank(message = "详细描述不能为空")
    private String description;

    /**
     * 地点
     */
    @Schema(description = "地点", required = true, example = "西一")
    @NotBlank(message = "地点不能为空")
    private String location;

    /**
     * 丢失/拾取时间
     */
    @Schema(description = "丢失/拾取时间", required = true, example = "2023-05-01 12:00:00")
    @NotNull(message = "丢失/拾取时间不能为空")
    private LocalDateTime happenTime;

    /**
     * 联系方式
     */
    @Schema(description = "联系方式", required = true, example = "13800000000")
    @NotBlank(message = "联系方式不能为空")
    private String contactMethod;
    @Schema(description = "图片URL列表")
    private List<String> imageUrls;

}
