package com.qg.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "图片AI响应 VO")
public class ImageAiResponseVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "图片AI分类")
    private String aiCategory; // 逗号分隔
    @Schema(description = "图片AIT标签")
    private String aiTags;     // 逗号分隔
    @Schema(description = "图片AI描述")
    private String aiDescription;
    @Schema(description = "图片AI状态")
    private String status;     // SUCCESS / FAILURE
    @Schema(description = "图片URL列表")
    private List<String> imageUrls;
}
