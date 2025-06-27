package com.light.translate.communicate.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.comm.Protocol;
import com.aliyun.oss.model.CannedAccessControlList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Component
public class OssUtil {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${aliyun.oss.fileHost}")
    private String fileHost;

    public String upload(InputStream inputStream, String originalFileName) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
            String objectName = fileHost + UUID.randomUUID() + suffix;

            ossClient.putObject(bucketName, objectName, inputStream);

            // 设置 URL 有效期为 30 天（单位：毫秒）
            Date expiration = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);
            URL signedUrl = ossClient.generatePresignedUrl(bucketName, objectName, expiration);

            return signedUrl.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            ossClient.shutdown();
        }
    }

    public String getUrl(String key) {
        // 创建ClientConfiguration时指定协议
        ClientBuilderConfiguration config = new ClientBuilderConfiguration();
        config.setProtocol(Protocol.HTTPS); // 强制使用HTTPS

        OSS ossClient = new OSSClientBuilder()
                .build(endpoint, accessKeyId, accessKeySecret, config);

        Date expiration = new Date(System.currentTimeMillis() + 1 * 60 * 60 * 1000);
        return ossClient.generatePresignedUrl(bucketName, key, expiration).toString();
    }
}

