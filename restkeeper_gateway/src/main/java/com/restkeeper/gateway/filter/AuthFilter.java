package com.restkeeper.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restkeeper.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关鉴权全局过滤器（过渡实现：自研 JWT）。
 * <p>阶段 5 将替换为 reactor Sa-Token 鉴权（sa-token-reactor），
 * 并与 web_common 的 Sa-Token 异常处理对齐。
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Value("#{'${gateway.excludedUrls}'.split(',')}")
    private List<String> excludedUrls;

    @Value("${gateway.secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpResponse response = exchange.getResponse();

        String path = exchange.getRequest().getURI().getPath();
        if (excludedUrls.contains(path)) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token != null && !token.isEmpty()) {
            JWTUtil.VerifyResult verifyResult = JWTUtil.verifyJwt(token, secret);
            if (verifyResult.isValidate()) {
                return chain.filter(exchange);
            } else {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("code", verifyResult.getCode());
                responseData.put("message", "验证失败");
                return responseError(response, responseData);
            }
        } else {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("code", 401);
            responseData.put("message", "非法请求");
            responseData.put("cause", "token is empty");
            return responseError(response, responseData);
        }
    }

    private Mono<Void> responseError(ServerHttpResponse response, Map<String, Object> responseData) {
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] data = new byte[0];
        try {
            data = objectMapper.writeValueAsBytes(responseData);
        } catch (Exception e) {
            // 序列化失败时返回空错误体，避免二次异常
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
