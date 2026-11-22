package com.restkeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.restkeeper"})
public class CashWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(CashWebApplication.class, args);
    }
}

