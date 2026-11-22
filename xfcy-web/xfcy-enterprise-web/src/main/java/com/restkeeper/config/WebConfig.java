package com.restkeeper.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * dev 本地上传文件静态映射：/upload/** -> ${oss.local.dir}
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${oss.local.dir:./upload}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/upload/**").addResourceLocations(location);
    }
}
