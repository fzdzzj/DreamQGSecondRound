package com.qg.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://127.0.0.1:5173", "http://localhost:5173")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(false)//允许携带cookie
                .maxAge(3600);
    }
}
