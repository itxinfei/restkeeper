package com.restkeeper.order.config;

import com.restkeeper.http.TenantContextClientInterceptor;
import com.restkeeper.store.service.ICreditCompanyUserService;
import com.restkeeper.store.service.ICreditLogService;
import com.restkeeper.store.service.ICreditService;
import com.restkeeper.store.service.IDishService;
import com.restkeeper.store.service.ISellCalculationService;
import com.restkeeper.store.service.ISetMealDishService;
import com.restkeeper.store.service.ISetMealService;
import com.restkeeper.store.service.ITableLogService;
import com.restkeeper.store.service.ITableService;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * 订单服务跨服务 HTTP Interface 客户端：替代 Dubbo @Reference。
 * 沽清、桌台、挂账等 7 个契约均由门店服务提供；服务名沿用 Nacos 既有注册名
 * （运维后置阶段再统一去 dubbo 化重命名）。
 */
@Configuration
public class OrderHttpClientConfig {

    /**
     * 门店服务名
     */
    private static final String STORE_SERVICE_URL = "http://store-dubbo-provider";

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                //透传 Seata 全局事务 XID，跨库分支才能随全局事务回滚
                .requestInterceptor(new SeataXidClientInterceptor());
    }

    /**
     * 基于门店服务地址构造契约代理；租户上下文头由 TenantContextClientInterceptor 透传。
     * 注意：Seata 全局事务 XID 的 HTTP 透传在中间件接入阶段补充。
     */
    private <T> T createStoreClient(Class<T> contractClass, RestClient.Builder restClientBuilder) {
        //clone 避免共享 Builder 时重复累积租户拦截器（负载均衡拦截器仍随 clone 保留）
        RestClient client = restClientBuilder.clone()
                .baseUrl(STORE_SERVICE_URL)
                .requestInterceptor(new TenantContextClientInterceptor())
                .build();
        return HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(client))
                .build()
                .createClient(contractClass);
    }

    @Bean
    public ISellCalculationService sellCalculationService(RestClient.Builder restClientBuilder) {
        return createStoreClient(ISellCalculationService.class, restClientBuilder);
    }

    @Bean
    public ITableService tableService(RestClient.Builder restClientBuilder) {
        return createStoreClient(ITableService.class, restClientBuilder);
    }

    @Bean
    public ICreditService creditService(RestClient.Builder restClientBuilder) {
        return createStoreClient(ICreditService.class, restClientBuilder);
    }

    @Bean
    public ICreditCompanyUserService creditCompanyUserService(RestClient.Builder restClientBuilder) {
        return createStoreClient(ICreditCompanyUserService.class, restClientBuilder);
    }

    @Bean
    public ICreditLogService creditLogService(RestClient.Builder restClientBuilder) {
        return createStoreClient(ICreditLogService.class, restClientBuilder);
    }

    @Bean
    public ITableLogService tableLogService(RestClient.Builder restClientBuilder) {
        return createStoreClient(ITableLogService.class, restClientBuilder);
    }

    @Bean
    public ISetMealDishService setMealDishService(RestClient.Builder restClientBuilder) {
        return createStoreClient(ISetMealDishService.class, restClientBuilder);
    }

    @Bean
    public IDishService dishService(RestClient.Builder restClientBuilder) {
        return createStoreClient(IDishService.class, restClientBuilder);
    }

    @Bean
    public ISetMealService setMealService(RestClient.Builder restClientBuilder) {
        return createStoreClient(ISetMealService.class, restClientBuilder);
    }
}
