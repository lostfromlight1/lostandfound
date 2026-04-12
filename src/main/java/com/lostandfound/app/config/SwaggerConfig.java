package com.lostandfound.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  private static final String BEARER_SCHEME_NAME = "BearerAuth";
  private static final String API_KEY_SCHEME_NAME = "ApiKeyAuth";
  private static final String API_KEY_HEADER = "X-API-KEY";

  @Bean
  OpenAPI customOpenAPI() {
    return new OpenAPI()
            .info(
                    new Info()
                            .title("Lost and Found API")
                            .version("1.0.0")
                            .description(
                                    "REST API documentation for the Lost and Found system. "
                                            + "Note: All endpoints require the X-API-KEY. Protected endpoints also require a JWT.")
                            .contact(new Contact().name("Admin").email("admin@lostandfound.com")))

            // Chaining them in the same SecurityRequirement means Logical AND (Both are applied)
            .addSecurityItem(
                    new SecurityRequirement()
                            .addList(API_KEY_SCHEME_NAME)
                            .addList(BEARER_SCHEME_NAME))

            .components(
                    new Components()
                            // 1. The JWT Bearer Scheme
                            .addSecuritySchemes(
                                    BEARER_SCHEME_NAME,
                                    new SecurityScheme()
                                            .name(BEARER_SCHEME_NAME)
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme("bearer")
                                            .bearerFormat("JWT")
                                            .description("Enter your JWT Token here. Example: 'eyJh...'"))

                            // 2. The Gateway API Key Scheme
                            .addSecuritySchemes(
                                    API_KEY_SCHEME_NAME,
                                    new SecurityScheme()
                                            .name(API_KEY_HEADER) // The exact header name to send
                                            .type(SecurityScheme.Type.APIKEY)
                                            .in(SecurityScheme.In.HEADER)
                                            .description("Enter your Master API Key (Required for all requests)")));
  }
}