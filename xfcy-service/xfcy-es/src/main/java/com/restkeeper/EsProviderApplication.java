package com.restkeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 检索服务：K16 起只读 store 库（spring-jdbc + JdbcTemplate）用于索引回填/全量重建，
 * 不引入 MyBatis-Plus 与租户拦截，数据源配置见 Nacos dataId es-dubbo-provider.yml。
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.restkeeper"})
public class EsProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsProviderApplication.class, args);
    }
}
