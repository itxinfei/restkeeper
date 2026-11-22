package com.restkeeper.config;

import com.restkeeper.config.HttpClientFactory;
import com.restkeeper.service.IDishSearchService;
import com.restkeeper.service.IOrderService;
import com.restkeeper.service.IReverseOrderService;
import com.restkeeper.store.service.IDishService;
import com.restkeeper.store.service.ISellCalculationService;
import com.restkeeper.store.service.ISetMealService;
import com.restkeeper.store.service.IStaffService;
import com.restkeeper.store.service.ITableAreaService;
import com.restkeeper.store.service.ITableLogService;
import com.restkeeper.store.service.ITableService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * cash_web 的 provider 契约代理声明。
 */
@Configuration
public class CashWebClientConfig {

    private static final String STORE = "store-dubbo-provider";
    private static final String ORDER = "order-dubbo-provider";
    private static final String ES = "es-dubbo-provider";

    @Bean
    IStaffService staffService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, IStaffService.class);
    }

    @Bean
    ITableAreaService tableAreaService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, ITableAreaService.class);
    }

    @Bean
    ITableLogService tableLogService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, ITableLogService.class);
    }

    @Bean
    ITableService tableService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, ITableService.class);
    }

    @Bean
    ISellCalculationService sellCalculationService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, ISellCalculationService.class);
    }

    @Bean
    IDishService dishService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, IDishService.class);
    }

    @Bean
    ISetMealService setMealService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, ISetMealService.class);
    }

    @Bean
    IOrderService orderService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, ORDER, IOrderService.class);
    }

    @Bean
    IReverseOrderService reverseOrderService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, ORDER, IReverseOrderService.class);
    }

    @Bean
    IDishSearchService dishSearchService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, ES, IDishSearchService.class);
    }
}
