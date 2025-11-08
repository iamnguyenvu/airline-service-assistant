package io.github.nguyenvu.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SpringAiConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        log.info("Initializing ChatClient with model bean: {}", chatModel.getClass().getSimpleName());
        return ChatClient.builder(chatModel)
            .defaultSystem("You are an airline assistant. Be concise and cite policies when relevant.")
            .build();
    }
}

