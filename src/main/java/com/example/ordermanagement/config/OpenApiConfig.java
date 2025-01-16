package com.example.ordermanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Management API")
                        .description("E-Commerce Order Management System API Documentation")
                        .version("1.0")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com")));
    }
} 