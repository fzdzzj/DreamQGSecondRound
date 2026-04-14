package com.qg.server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Swagger 配置类
 */
@Configuration
public class OpenApiConfig {
    /**
     * 创建 Swagger 分组接口
     * 用于将所有接口分组为 lostfound-server
     *
     * @return 分组接口对象
     */
    @Bean
    public GroupedOpenApi serverApi() {
        return GroupedOpenApi.builder()//创建分组接口对象
                .group("lostfound-server")//分组名称
                .pathsToMatch("/**")//匹配所有路径
                .build();
    }

    /**
     * 创建 OpenAPI 对象
     * 用于创建全局的 OpenAPI 对象
     *
     * @return OpenAPI 对象
     */
    @Bean
    @Primary
    public OpenAPI customOpenAPI_v2() {
        // 创建 OpenAPI 对象
        return new OpenAPI()//创建 OpenAPI 对象
                .info(new Info()
                        .title("校园失物招领系统 API V2")
                        .version("1.0")
                        .description("基于 Spring Boot + MyBatis-Plus + OpenAI"))//创建 Info 对象 设置标题、版本、描述
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ))//创建 Components 对象 添加 JWT 认证方案
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));//添加 JWT 认证方案
    }

}
