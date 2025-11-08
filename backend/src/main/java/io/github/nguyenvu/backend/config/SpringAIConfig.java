package io.github.nguyenvu.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SpringAiConfig {
    @Bean
    public ChatClient chatClient(@Qualifier("googleGenAiChatModel") ChatModel model) {
        log.info("Configuring ChatClient with model: {}", model);
        return ChatClient.builder(model)
                .defaultSystem("You are an airline assistant. " +
                        "Be concise and cite policy sections when relevant.")
                .build();
    }

}
