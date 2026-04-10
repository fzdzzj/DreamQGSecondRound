package com.qg.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ImageAiResponseVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String aiCategory; // 逗号分隔
    private String aiTags;     // 逗号分隔
    private String aiDescription;
    private String status;     // SUCCESS / FAILURE
    private List<String> imageUrls;
}
