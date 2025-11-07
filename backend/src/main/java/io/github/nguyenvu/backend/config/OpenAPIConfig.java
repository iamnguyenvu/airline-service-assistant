package io.github.nguyenvu.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Airline Service Assistant API")
                .version("1.0.0")
                .description("""
                    AI-powered flight search and customer service platform.
                    
                    Features:
                    - Intelligent flight search with multi-factor ranking
                    - AI chat with function calling (Gemini)
                    - Policy lookup with RAG (pgvector)
                    - Route analytics and price trends
                    """)
                .contact(new Contact()
                    .name("Nguyen Vu")
                    .url("https://github.com/iamnguyenvu")
                    .email("iamnguyenvu.gm@gmail.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Local development"),
                new Server()
                    .url("https://api.airline-assistant.com")
                    .description("Production")
            ));
    }
}
