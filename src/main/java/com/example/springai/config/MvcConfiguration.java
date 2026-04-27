package com.example.springai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置，主要处理跨域（CORS）策略。
 * 允许前端任意域名调用后端 API。
 */
@Configuration
public class MvcConfiguration implements WebMvcConfigurer {

    /**
     * 配置跨域映射，允许所有来源、方法和请求头。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition");
    }
}
