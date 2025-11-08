package io.github.nguyenvu.backend.config;

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
                .defaultSystem("You are an airline assistant. " +
                        "Be concise and cite policy sections when relevant.")
                .build();
    }

}
