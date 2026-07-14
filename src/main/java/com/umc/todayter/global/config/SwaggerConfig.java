package com.umc.todayter.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Todayter API")
                .description("Todayter 프로젝트 API 명세")
                .version("v0.0.1");

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        Components components = new Components()
                .addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        securityScheme
                );

        return new OpenAPI()
                .info(info)
                .addServersItem(new Server().url("/"))
                .components(components);
    }
}
