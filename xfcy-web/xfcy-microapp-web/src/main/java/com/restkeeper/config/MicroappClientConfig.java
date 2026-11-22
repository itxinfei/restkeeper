package com.restkeeper.config;

import com.restkeeper.service.IDishSearchService;
import com.restkeeper.service.IOrderService;
import com.restkeeper.store.service.IDishCategoryService;
import com.restkeeper.store.service.IDishService;
import com.restkeeper.store.service.ISetMealService;
import com.restkeeper.store.service.ITableAreaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * microapp_web 的 provider 契约代理声明（复用 web_common 的 @LoadBalanced Builder 与 HttpClientFactory）。
 */
@Configuration
public class MicroappClientConfig {

    private static final String STORE = "store-dubbo-provider";
    private static final String ORDER = "order-dubbo-provider";
    private static final String ES = "es-dubbo-provider";

    @Bean
    IDishCategoryService dishCategoryService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, IDishCategoryService.class);
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
    ITableAreaService tableAreaService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, STORE, ITableAreaService.class);
    }

    @Bean
    IOrderService orderService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, ORDER, IOrderService.class);
    }

    @Bean
    IDishSearchService dishSearchService(RestClient.Builder builder) {
        return HttpClientFactory.createClient(builder, ES, IDishSearchService.class);
    }
}
