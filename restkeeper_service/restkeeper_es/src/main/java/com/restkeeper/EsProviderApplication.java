package com.restkeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 检索服务不连数据库：classpath 无 spring-jdbc，MyBatis-Plus 与数据源自动配置均不会装配，
 * 因此无需像业务 provider 那样 exclude DataSourceAutoConfiguration（Boot 4 下该类已拆出 autoconfigure 主包）。
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.restkeeper"})
public class EsProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsProviderApplication.class, args);
    }
}
