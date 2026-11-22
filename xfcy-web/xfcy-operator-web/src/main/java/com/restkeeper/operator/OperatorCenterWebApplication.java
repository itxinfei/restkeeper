package com.restkeeper.operator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.restkeeper"})
@EnableDiscoveryClient //声明当前服务是注册中心的客户端
public class OperatorCenterWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(OperatorCenterWebApplication.class, args);
    }
}
