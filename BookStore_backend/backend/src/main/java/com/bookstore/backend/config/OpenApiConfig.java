package com.bookstore.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookstoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BookStore API")
                        .description("OpenAPI documentation for the BookStore backend.")
                        .version("v1.0.0")
                        .contact(new Contact().name("BookStore Team"))
                        .license(new License().name("Internal Use")));
    }
}
