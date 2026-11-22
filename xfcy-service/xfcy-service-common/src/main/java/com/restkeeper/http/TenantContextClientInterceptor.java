package com.restkeeper.http;

import com.restkeeper.tenant.TenantContext;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * HTTP Interface / RestClient 出站拦截器：
 * 替代 Dubbo 时代的 RpcContext 附件，把租户与登录人上下文写入请求头透传给下游。
 */
public class TenantContextClientInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        setIfPresent(request, HttpHeaderNames.X_TENANT_SHOP_ID, TenantContext.getShopId());
        setIfPresent(request, HttpHeaderNames.X_TENANT_STORE_ID, TenantContext.getStoreId());
        setIfPresent(request, HttpHeaderNames.X_LOGIN_USER_ID, TenantContext.getLoginUserId());
        setIfPresent(request, HttpHeaderNames.X_LOGIN_USER_NAME, TenantContext.getLoginUserName());
        return execution.execute(request, body);
    }

    /**
     * TenantContext 取不到值时返回字符串 "null"，此处统一判空，避免把 "null" 透传下去
     */
    private void setIfPresent(HttpRequest request, String headerName, String value) {
        if (value != null && !value.isEmpty() && !"null".equals(value)) {
            request.getHeaders().set(headerName, value);
        }
    }
}
