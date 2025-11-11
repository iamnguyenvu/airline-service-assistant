package io.github.nguyenvu.backend.config;

import io.github.nguyenvu.backend.ai.tool.EstimateCO2Tool;
import io.github.nguyenvu.backend.ai.tool.LiveStatusTool;
import io.github.nguyenvu.backend.ai.tool.RagPolicyTool;
import io.github.nguyenvu.backend.ai.tool.SearchFlightsTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAiConfig {
    private static final Logger log = LoggerFactory.getLogger(SpringAiConfig.class);
    
    @Bean
    public ChatClient chatClient(@Qualifier("googleGenAiChatModel") ChatModel model) {
        log.info("Configuring ChatClient with model: {}", model);
        return ChatClient.builder(model)
                .defaultSystem("""
                        You are an airline assistant.
                        Use tools for factual data (flights/policies).
                        Always provide citations for policies. Avoid hallucination.
                        """)
                .build();
    }

}
