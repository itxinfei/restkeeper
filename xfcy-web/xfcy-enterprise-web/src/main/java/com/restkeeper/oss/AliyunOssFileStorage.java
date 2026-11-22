package com.restkeeper.oss;

import com.aliyun.oss.OSSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * 阿里云 OSS 存储实现（非 dev 环境）。
 */
@Service
@Profile("!dev")
public class AliyunOssFileStorage implements FileStorage {

    private final OSSClient ossClient;

    @Value("${oss.bucketName}")
    private String bucketName;

    @Value("${oss.endpoint}")
    private String endpoint;

    public AliyunOssFileStorage(OSSClient ossClient) {
        this.ossClient = ossClient;
    }

    @Override
    public String store(String fileName, InputStream input) {
        ossClient.putObject(bucketName, fileName, input);
        return "https://" + bucketName + "." + endpoint + "/" + fileName;
    }
}
