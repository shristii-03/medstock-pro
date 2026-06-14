package com.medstockpro.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MedStock Pro API")
                        .description(
                                "Pharmacy Inventory Management System — " +
                                "Built for a real family-owned pharmacy " +
                                "in Indore to replace manual Excel-based " +
                                "inventory tracking.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MedStock Pro")
                                .email("admin@medstockpro.com")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes(
                                "Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "Enter JWT token")));
    }
}