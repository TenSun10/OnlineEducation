package com.tenxi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "在线教育平台API",
                version = "1.0",
                description = "基于Spring Cloud微服务架构的在线教育平台API文档"
        )
)
public class OpenApiConfig {

    /**
     * 往文档里追加“全局安全组件”
     * 如果某个接口打了安全注解（如 @SecurityRequirement(name = "bearer-jwt")）
     * 那么调用它时请在请求头里带上 Authorization: Bearer <jwt>”。
     * @return
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));

    }
}
