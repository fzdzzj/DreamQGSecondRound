package com.qg.server.controller.item;

import com.qg.common.result.Result;
import com.qg.common.util.AliyunOSSOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 文件上传接口
 */
@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Tag(name = "文件上传接口", description = "图片/文件上传至阿里云OSS，返回URL地址")
public class UploadController {

    private final AliyunOSSOperator aliyunOSSOperator;

    @Value("${aliyun.oss.allowed-types}")
    private String allowedTypesStr;

    @Value("${aliyun.oss.max-file-size}")
    private DataSize maxFileSize;

    /**
     * 文件上传
     *
     * @param file 上传文件
     * @return 上传后的文件URL
     * @throws Exception 上传过程中可能抛出的异常
     */
    @PostMapping("/upload")
    @Operation(summary = "文件上传", description = "支持图片格式，上传成功返回OSS访问地址")
    public Result<String> upload(
            @Parameter(description = "上传文件", required = true)
            @RequestPart("file") MultipartFile file) throws Exception {
        // 校验上传文件是否为空
        if (file == null || file.isEmpty()) {
            log.warn("上传文件不能为空");
            return Result.error(400, "上传文件不能为空");
        }

        log.info("文件上传请求，文件名={}, 大小={}KB",
                file.getOriginalFilename(), file.getSize() / 1024);
        // 校验上传文件类型是否支持
        List<String> allowedTypes = Arrays.asList(allowedTypesStr.split(","));
        String contentType = file.getContentType();

        if (contentType == null || !allowedTypes.contains(contentType)) {
            log.warn("不支持的文件类型：{}", contentType);
            return Result.error(400, "不支持的文件类型：" + contentType);
        }
        // 校验上传文件大小是否超过最大限制
        if (file.getSize() > maxFileSize.toBytes()) {
            log.warn("文件大小超限，当前={}KB，最大={}MB",
                    file.getSize() / 1024, maxFileSize.toMegabytes());
            return Result.error(413, "文件大小不能超过 " + maxFileSize.toMegabytes() + "MB");
        }
        // 上传文件至OSS
        String url = aliyunOSSOperator.upload(file.getBytes(), file.getOriginalFilename());
        log.info("文件上传成功，URL={}", url);
        return Result.success(url);
    }
}
