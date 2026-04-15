package com.qg.server.config;

import org.apache.catalina.filters.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许前端地址（你本地 3000 端口）
        config.addAllowedOrigin("http://localhost:3000");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有请求方式（GET,POST,PUT,DELETE 等）
        config.addAllowedMethod("*");
        // 允许携带 Cookie / Token（关键！）
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter();
    }
}