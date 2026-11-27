package com.restkeeper.config;

import com.restkeeper.SpringUtil;
import com.restkeeper.http.ResultStatusInterceptor;
import com.restkeeper.http.TenantContextClientInterceptor;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;

/**
 * provider 契约代理工厂：基于 @LoadBalanced RestClient.Builder 创建 HTTP Interface 代理。
 * <p>每次 clone 基础 Builder 再挂拦截器，避免多个客户端共用 Builder 时拦截器重复累积；
 * 出站经 {@link TenantContextClientInterceptor} 透传租户与登录人上下文。
 * <p>代理方法调用经 Resilience4j 重试 + Spring Cloud CircuitBreaker（熔断/超时）装饰，
 * 熔断实例按服务名隔离；若容器无 CircuitBreakerFactory（非 web 应用）则降级为不装饰。
 */
public final class HttpClientFactory {

    private HttpClientFactory() {
    }

    public static <T> T createClient(RestClient.Builder builder, String serviceName, Class<T> type) {
        RestClient restClient = builder.clone()
                .baseUrl("http://" + serviceName)
                .requestInterceptor(new TenantContextClientInterceptor())
                //识别 provider 的 Result 错误信封（HTTP 200 + status!=200），避免静默空结果/丢失原因
                .requestInterceptor(new ResultStatusInterceptor())
                .build();
        T client = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(type);
        return wrapWithResilience4j(client, serviceName, type);
    }

    @SuppressWarnings("unchecked")
    private static <T> T wrapWithResilience4j(T client, String serviceName, Class<T> type) {
        CircuitBreakerFactory<?, ?> factory;
        try {
            factory = (CircuitBreakerFactory<?, ?>) SpringUtil.getBeanByType(CircuitBreakerFactory.class);
        } catch (Exception e) {
            // 无韧性 starter（如 provider 间直连场景）→ 不装饰
            return client;
        }
        CircuitBreaker circuitBreaker = factory.create(serviceName);
        Retry retry = Retry.of(serviceName, RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(300))
                .build());

        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return method.invoke(client, args);
                    }
                    return retry.executeSupplier(() -> circuitBreaker.run(
                            () -> invoke(client, method, args),
                            throwable -> { throw asUnchecked(throwable); }));
                });
    }

    private static Object invoke(Object client, Method method, Object[] args) {
        try {
            return method.invoke(client, args);
        } catch (InvocationTargetException e) {
            throw asUnchecked(e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static RuntimeException asUnchecked(Throwable throwable) {
        if (throwable instanceof RuntimeException re) {
            return re;
        }
        if (throwable instanceof Error err) {
            throw err;
        }
        return new RuntimeException(throwable);
    }
}
