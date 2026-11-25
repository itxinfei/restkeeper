package com.restkeeper.controller;

import com.restkeeper.tenant.TenantContext;
import org.springframework.util.StringUtils;

/**
 * C 端匿名浏览菜单时的租户上下文还原：由请求 query 参数携带 shopId/storeId，
 * 写入 TenantContext 供 BFF → provider 的 HTTP Interface 客户端出站透传。
 * 已登录场景下网关会注入同名请求头，由 TenantContextHeaderFilter 还原，本兜底不覆盖已有值之外的行为。
 */
public final class TenantQuerySupport {

    private TenantQuerySupport() {
    }

    public static void apply(String shopId, String storeId) {
        if (StringUtils.hasText(shopId)) {
            TenantContext.addAttachment("shopId", shopId);
        }
        if (StringUtils.hasText(storeId)) {
            TenantContext.addAttachment("storeId", storeId);
        }
    }
}
