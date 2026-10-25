package com.example.food_delivery_app.config;

import com.example.food_delivery_app.common.Constants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(Constants.FOOD_DELIVERY_API)
                        .description(Constants.API_DOCUMENTATION)
                        .version(Constants.API_VERSION)
                )
                .addSecurityItem(new SecurityRequirement().addList(Constants.BEARER_AUTHENTICATION))
                .components(new Components()
                        .addSecuritySchemes(Constants.BEARER_AUTHENTICATION, createSecurityScheme())
                );
    }

    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .name(Constants.BEARER_AUTHENTICATION)
                .type(SecurityScheme.Type.HTTP)
                .scheme(Constants.BEARER_SMALL)
                .bearerFormat(Constants.JWT);
    }
}