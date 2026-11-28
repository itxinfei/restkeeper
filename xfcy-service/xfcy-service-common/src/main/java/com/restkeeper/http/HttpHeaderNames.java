package com.restkeeper.http;

/**
 * 服务间 HTTP 调用的上下文请求头常量（网关 / web / provider 统一）。
 */
public final class HttpHeaderNames {

    /** 集团（商户）ID */
    public static final String X_TENANT_SHOP_ID = "x-tenant-shop-id";
    /** 门店 ID */
    public static final String X_TENANT_STORE_ID = "x-tenant-store-id";
    /** 登录用户 ID（四个租户/登录头统一 x-tenant-* 前缀，网关与 BFF/provider 两侧共用） */
    public static final String X_TENANT_USER_ID = "x-tenant-user-id";
    /** 登录用户姓名（非 ASCII 需 B64: 前缀编码） */
    public static final String X_TENANT_USER_NAME = "x-tenant-user-name";

    private HttpHeaderNames() {
    }
}
