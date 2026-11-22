package com.restkeeper.config;

import com.restkeeper.http.TenantContextClientInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * provider 契约代理工厂：基于 @LoadBalanced RestClient.Builder 创建 HTTP Interface 代理。
 * <p>每次 clone 基础 Builder 再挂拦截器，避免多个客户端共用 Builder 时拦截器重复累积；
 * 出站经 {@link TenantContextClientInterceptor} 透传租户与登录人上下文。
 */
public final class HttpClientFactory {

    private HttpClientFactory() {
    }

    public static <T> T createClient(RestClient.Builder builder, String serviceName, Class<T> type) {
        RestClient restClient = builder.clone()
                .baseUrl("http://" + serviceName)
                .requestInterceptor(new TenantContextClientInterceptor())
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(type);
    }
}
