package com.restkeeper.response.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置。
 * 鉴权拦截器已由 Sa-Token 接管（网关统一鉴权），此处仅保留跨域配置；
 * Boot 4 下 allowCredentials(true) 不能再与 allowedOrigins("*") 联用，改用 allowedOriginPatterns。
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "PUT", "DELETE", "POST", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
