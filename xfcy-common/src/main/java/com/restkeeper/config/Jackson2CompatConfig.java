package com.restkeeper.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 兼容提供 Jackson 2 的 {@link com.fasterxml.jackson.databind.ObjectMapper}。
 * <p>Spring Boot 4.1 起 HTTP 消息转换默认走 Jackson 3（tools.jackson），
 * 容器内不再有 Jackson 2 的 ObjectMapper bean；而部分既有代码
 * （如 {@code DishIndexConsumer}、网关 {@code AuthFilter}）按 Jackson 2 类型注入，
 * 这里补一个标准实例，避免启动期依赖解析失败。
 * <p>经 {@code META-INF/spring/...AutoConfiguration.imports} 自动装配，
 * 不依赖各应用的包扫描范围；容器已有 ObjectMapper 时不重复定义。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(com.fasterxml.jackson.databind.ObjectMapper.class)
public class Jackson2CompatConfig {

    @Bean
    @ConditionalOnMissingBean(com.fasterxml.jackson.databind.ObjectMapper.class)
    public com.fasterxml.jackson.databind.ObjectMapper jackson2ObjectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper();
    }
}
