package com.restkeeper.http;

import com.restkeeper.tenant.TenantContext;
import com.restkeeper.utils.HeaderTextCodec;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 入站过滤器：从请求头还原租户与登录人上下文到 TenantContext，
 * 替代 Dubbo 时代基于 RpcContext 附件的 TenantAspect。
 * 请求结束后清理 ThreadLocal，避免虚拟线程 / 线程池复用造成串号。
 */
@Component
@Order(1)
public class TenantContextHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            putIfPresent(request, HttpHeaderNames.X_TENANT_SHOP_ID, "shopId");
            putIfPresent(request, HttpHeaderNames.X_TENANT_STORE_ID, "storeId");
            putIfPresent(request, HttpHeaderNames.X_LOGIN_USER_ID, "loginUserId");
            putIfPresent(request, HttpHeaderNames.X_LOGIN_USER_NAME, "loginUserName");
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private void putIfPresent(HttpServletRequest request, String headerName, String contextKey) {
        String value = request.getHeader(headerName);
        if (StringUtils.hasText(value)) {
            //登录人姓名经网关注入时做了 Base64 编码，此处还原；其余头为纯 ASCII 原样透传
            if (HttpHeaderNames.X_LOGIN_USER_NAME.equals(headerName)) {
                value = HeaderTextCodec.decode(value);
            }
            TenantContext.addAttachment(contextKey, value);
        }
    }
}
