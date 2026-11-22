package com.restkeeper.operator.config;

import com.restkeeper.config.HttpClientFactory;
import com.restkeeper.operator.service.IEnterpriseAccountService;
import com.restkeeper.operator.service.IOperatorUserService;
import com.restkeeper.operator.service.ISysDictService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * operator_web 的 provider 契约代理声明。
 */
@Configuration
public class OperatorClientConfig {

    private static final String SERVICE = "operator-dubbo-provider";

    @Bean
    IOperatorUserService operatorUserService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, SERVICE, IOperatorUserService.class);
    }

    @Bean
    IEnterpriseAccountService enterpriseAccountService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, SERVICE, IEnterpriseAccountService.class);
    }

    @Bean
    ISysDictService sysDictService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, SERVICE, ISysDictService.class);
    }
}
