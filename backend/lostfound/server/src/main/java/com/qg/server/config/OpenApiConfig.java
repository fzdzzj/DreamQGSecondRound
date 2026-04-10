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

@Configuration
public class OpenApiConfig {

    // 分组接口
    @Bean
    public GroupedOpenApi serverApi() {
        return GroupedOpenApi.builder()
                .group("lostfound-server")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    @Primary
    public OpenAPI customOpenAPI_v2() {  // 改名
        return new OpenAPI()
                .info(new Info()
                        .title("校园失物招领系统 API V2")
                        .version("1.0")
                        .description("基于 Spring Boot + MyBatis-Plus + OpenAI"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

}
