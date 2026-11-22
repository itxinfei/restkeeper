package com.restkeeper.store.config;

import com.restkeeper.http.TenantContextClientInterceptor;
import com.restkeeper.operator.service.ISysDictService;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * 跨服务 HTTP Interface 客户端：替代 Dubbo @Reference。
 * 服务名沿用 Nacos 既有注册名（运维后置阶段再统一去 dubbo 化重命名）。
 */
@Configuration
public class StoreHttpClientConfig {

    /**
     * operator 运营端服务名
     */
    private static final String OPERATOR_SERVICE_URL = "http://operator-dubbo-provider";

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public ISysDictService sysDictService(RestClient.Builder restClientBuilder) {
        RestClient client = restClientBuilder
                .baseUrl(OPERATOR_SERVICE_URL)
                .requestInterceptor(new TenantContextClientInterceptor())
                .build();
        return HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(client))
                .build()
                .createClient(ISysDictService.class);
    }
}
