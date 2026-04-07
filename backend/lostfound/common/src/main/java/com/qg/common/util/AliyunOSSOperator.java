package com.qg.common.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.qg.common.constant.MessageConstant;
import com.qg.common.exception.BaseException;
import com.qg.common.properties.AliyunOSSProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AliyunOSSOperator {

    private final AliyunOSSProperties aliyunOSSProperties;
    private static final DateTimeFormatter DIR_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");

    public String upload(byte[] content, String originalFilename) {
        if (content == null || content.length == 0) {
            throw new BaseException(500, "文件不能为空");
        }

        String endpoint = aliyunOSSProperties.getEndpoint();
        String region = aliyunOSSProperties.getRegion();
        String bucketName = aliyunOSSProperties.getBucketName();
        String accessKeyId = aliyunOSSProperties.getAccessKeyId();
        String accessKeySecret = aliyunOSSProperties.getAccessKeySecret();

        // 生成文件路径
        String dir = LocalDate.now().format(DIR_FORMATTER);
        String ext = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        String objectName = dir + "/" + fileName;

        OSS ossClient = null;
        try (InputStream is = new ByteArrayInputStream(content)) {
            // 创建OSSClient实例
            CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
            // 创建ClientBuilderConfiguration
            com.aliyun.oss.ClientBuilderConfiguration conf = new com.aliyun.oss.ClientBuilderConfiguration();
            conf.setSignatureVersion(SignVersion.V4);
            /// 创建OSSClient实例
            ossClient = OSSClientBuilder.create()
                    .endpoint(endpoint)
                    .credentialsProvider(credentialsProvider)
                    .clientConfiguration(conf)
                    .region(region)
                    .build();

            // 上传
            ossClient.putObject(bucketName, objectName, is);
            log.info("OSS上传成功：{}", objectName);

            return buildUrl(endpoint, bucketName, objectName);

        } catch (Exception e) {
            log.error("OSS上传失败", e);
            throw new BaseException(500, MessageConstant.IMAGE_UPLOAD_FAILED);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private String buildUrl(String endpoint, String bucketName, String objectName) {
        if (endpoint.startsWith("http://")) {
            return "http://" + bucketName + "." + endpoint.substring(7) + "/" + objectName;
        } else if (endpoint.startsWith("https://")) {
            return "https://" + bucketName + "." + endpoint.substring(8) + "/" + objectName;
        } else {
            return "https://" + bucketName + "." + endpoint + "/" + objectName;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".png";
        return filename.substring(filename.lastIndexOf("."));
    }
}