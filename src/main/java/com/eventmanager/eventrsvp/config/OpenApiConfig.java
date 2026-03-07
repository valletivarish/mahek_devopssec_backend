package com.eventmanager.eventrsvp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for auto-generating API documentation.
 * Configures JWT Bearer authentication scheme so Swagger UI allows testing
 * authenticated endpoints with a token input field.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates the OpenAPI specification with application metadata
     * and JWT security scheme for the Swagger UI.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Event RSVP and Attendance Manager API")
                        .version("1.0.0")
                        .description("REST API for managing events, RSVPs, attendees, "
                                + "categories, and check-ins with attendance forecasting")
                        .contact(new Contact()
                                .name("Mahek Naaz")
                                .email("mahek.naaz@student.ncirl.ie")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Bearer Authentication")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
