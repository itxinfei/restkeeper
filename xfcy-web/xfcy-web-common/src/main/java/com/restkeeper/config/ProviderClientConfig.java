package com.restkeeper.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * web 侧公共 HTTP 客户端配置：提供 @LoadBalanced 的 RestClient.Builder，
 * 由各 web 模块按 Nacos 服务名创建 provider 契约代理（见 {@link HttpClientFactory}）。
 */
@Configuration
public class ProviderClientConfig {

    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }
}
