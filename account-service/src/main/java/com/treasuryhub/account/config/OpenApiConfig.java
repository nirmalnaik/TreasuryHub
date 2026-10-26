package com.treasuryhub.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accountServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("TreasuryHub Account API")
                .description("API for creating and looking up corporate bank accounts.")
                .version("v1"));
    }
}
