package com.treasuryhub.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "treasuryhub.api-docs.ui-origin")
public class ApiDocsCorsConfig implements WebMvcConfigurer {

    private final String uiOrigin;

    public ApiDocsCorsConfig(@Value("${treasuryhub.api-docs.ui-origin}") String uiOrigin) {
        this.uiOrigin = uiOrigin;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/v3/api-docs/**")
                .allowedOrigins(uiOrigin)
                .allowedMethods(HttpMethod.GET.name())
                .allowedHeaders("Accept", "Content-Type")
                .maxAge(3600);
    }
}
