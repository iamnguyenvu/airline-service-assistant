package io.github.nguyenvu.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for CORS and other MVC settings.
 * CORS origins are configurable via environment variables.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization", "X-Requested-With", "Accept")
                .exposedHeaders("X-RateLimit-Remaining", "X-RateLimit-Limit", "Retry-After")
                .allowCredentials(true)
                .maxAge(3600);

        // Allow Swagger UI CORS
        registry.addMapping("/v3/api-docs/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET");

        registry.addMapping("/swagger-ui/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET");
    }
}
