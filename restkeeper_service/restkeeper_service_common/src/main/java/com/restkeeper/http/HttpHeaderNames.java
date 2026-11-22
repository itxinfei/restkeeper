package com.restkeeper.http;

/**
 * 服务间 HTTP 调用的上下文请求头常量（网关 / web / provider 统一）。
 */
public final class HttpHeaderNames {

    /** 集团（商户）ID */
    public static final String X_TENANT_SHOP_ID = "x-tenant-shop-id";
    /** 门店 ID */
    public static final String X_TENANT_STORE_ID = "x-tenant-store-id";
    /** 登录用户 ID */
    public static final String X_LOGIN_USER_ID = "x-login-user-id";
    /** 登录用户姓名 */
    public static final String X_LOGIN_USER_NAME = "x-login-user-name";

    private HttpHeaderNames() {
    }
}
