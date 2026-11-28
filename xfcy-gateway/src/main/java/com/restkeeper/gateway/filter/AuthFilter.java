package com.restkeeper.gateway.filter;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restkeeper.utils.HeaderTextCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关鉴权全局过滤器（阶段 5：Sa-Token reactor 校验，替换自研 JWT）。
 * <p>校验通过后从会话注入租户上下文请求头（x-tenant-shop-id 等），
 * provider 侧 TenantContextHeaderFilter 据此还原 ThreadLocal。
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    /**
     * 放行清单支持 Ant 风格（如 /microApp/**）；旧配置为精确路径，两者都可匹配。
     */
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Value("#{'${gateway.excludedUrls:}'.split(',')}")
    private List<String> excludedUrls;

    /**
     * C 端独立 StpLogic：microapp-web 用 "microapp" 签发，会话 Redis 共享。
     * 白名单路径（/microApp/**）带其令牌时据此还原租户上下文。
     * 注意：不注册为 Spring Bean——sa-token starter 会把容器内所有 StpLogic Bean
     * 依次注入并覆盖 StpUtil 的默认 logic（SaBeanInject#setStpLogic），
     * 一旦注册，B 端默认 logic 校验会错位到 microapp 前缀而失效。
     */
    private final StpLogic microappStpLogic = new StpLogic("microapp");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();

        String path = exchange.getRequest().getURI().getPath();
        boolean excluded = excludedUrls.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
        if (excluded) {
            //白名单允许匿名；但若带有效 token，仍注入会话租户头，保证 C 端已登录用户下游可还原上下文。
            //B 端默认 logic 与 C 端 microapp logic 任一校验通过即可。
            return SaReactorSyncHolder.setContext(exchange, () -> {
                try {
                    if (exchange.getRequest().getHeaders().getFirst("Authorization") != null) {
                        try {
                            StpUtil.checkLogin();
                            return chain.filter(injectTenantHeaders(exchange, StpUtil.getSession()));
                        } catch (NotLoginException ignored) {
                            //默认 logic 不识别该令牌，继续尝试 C 端 microapp logic
                        }
                        microappStpLogic.checkLogin();
                        return chain.filter(injectTenantHeaders(exchange, microappStpLogic.getSession()));
                    }
                } catch (Exception ignored) {
                    //匿名访问或令牌失效：无租户上下文属预期，直接放行
                }
                return chain.filter(exchange);
            });
        }

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (token == null || token.isEmpty()) {
            return responseError(response, 401, "非法请求", "token is empty");
        }

        try {
            return SaReactorSyncHolder.setContext(exchange, () -> {
                StpUtil.checkLogin();
                return chain.filter(injectTenantHeaders(exchange, StpUtil.getSession()));
            });
        } catch (NotLoginException e) {
            return responseError(response, 401, "验证失败", "未登录或登录已过期");
        }
    }

    /**
     * 从 Sa-Token 会话注入租户上下文请求头，供下游 provider 还原 ThreadLocal
     */
    private ServerWebExchange injectTenantHeaders(ServerWebExchange exchange, SaSession session) {
        String shopId = (String) session.get("shopId");
        String storeId = (String) session.get("storeId");
        String loginUserId = (String) session.get("loginUserId");
        String loginUserName = (String) session.get("loginUserName");

        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .headers(headers -> {
                    if (shopId != null) {
                        headers.set("x-tenant-shop-id", shopId);
                    }
                    if (storeId != null) {
                        headers.set("x-tenant-store-id", storeId);
                    }
                    if (loginUserId != null) {
                        headers.set("x-tenant-user-id", loginUserId);
                    }
                    if (loginUserName != null) {
                        //头值按 ISO-8859-1 传输，中文姓名需 Base64 编码，provider 侧解码还原
                        headers.set("x-tenant-user-name", HeaderTextCodec.encode(loginUserName));
                    }
                })
                .build();
        return exchange.mutate().request(mutated).build();
    }

    private Mono<Void> responseError(ServerHttpResponse response, int code, String message, String cause) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("code", code);
        responseData.put("message", message);
        responseData.put("cause", cause);

        byte[] data;
        try {
            data = new ObjectMapper().writeValueAsBytes(responseData);
        } catch (Exception e) {
            data = new byte[0];
        }

        DataBuffer buffer = response.bufferFactory().wrap(data);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=utf-8");
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
