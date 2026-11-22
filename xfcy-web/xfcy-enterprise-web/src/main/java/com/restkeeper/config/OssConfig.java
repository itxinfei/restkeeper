package com.restkeeper.config;

import com.aliyun.oss.OSSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * OSS 客户端装配（阶段 4：替换 spring-cloud-starter-alicloud-oss 的自动配置）。
 * <p>dev 环境走 {@link com.restkeeper.oss.LocalFileStorage}，不装配 OSSClient。
 */
@Configuration
public class OssConfig {

    @Bean
    @Profile("!dev")
    public OSSClient ossClient(@Value("${oss.endpoint}") String endpoint,
                               @Value("${oss.accessKeyId}") String accessKeyId,
                               @Value("${oss.accessKeySecret}") String accessKeySecret) {
        return new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }
}
