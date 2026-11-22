package com.restkeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 点餐 H5 / 微信小程序 BFF（阶段 6）。
 * <p>端口 8087，context-path /microApp；复用 store/order/es 的 HTTP Interface 契约，不新建数据访问层；
 * C 端会话使用独立 Sa-Token StpLogic（microapp），与 B 端隔离。
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.restkeeper"})
public class MicroappWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroappWebApplication.class, args);
    }
}
