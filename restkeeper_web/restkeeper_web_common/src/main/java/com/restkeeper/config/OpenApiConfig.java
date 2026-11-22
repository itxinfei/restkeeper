package com.restkeeper.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springdoc 公共配置：沿用前端契约，鉴权头为小写 authorization。
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_NAME = "authorization";

    @Bean
    public OpenAPI restkeeperOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("RestKeeper 餐掌柜 API").version("1.0"))
                .components(new Components().addSecuritySchemes(SECURITY_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(SECURITY_NAME)))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_NAME));
    }
}
