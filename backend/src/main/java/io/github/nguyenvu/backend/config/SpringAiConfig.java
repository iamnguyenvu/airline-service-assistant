package io.github.nguyenvu.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class SpringAiConfig {
    private static final Logger log = LoggerFactory.getLogger(SpringAiConfig.class);
    
    private static final String SYSTEM_PROMPT = """
            You are an airline customer service assistant.
            You help customers with flight search, policy questions, and travel information.
            
            Always provide accurate information. For policy questions, mention that information comes from official airline documents.
            Respond in Vietnamese when the customer writes in Vietnamese.
            Be helpful, friendly, and professional.
            """;
    
    @Bean
    @Primary
    public ChatClient chatClient(List<ChatModel> chatModels) {
        if (chatModels == null || chatModels.isEmpty()) {
            log.warn("No ChatModel beans found. Chat functionality will be limited.");
            log.warn("Please configure either:");
            log.warn("1. Google GenAI: Set GEMINI_API_KEY environment variable (Google AI Studio - FREE)");
            log.warn("   Get API key from: https://aistudio.google.com/app/apikey");
            log.warn("2. Ollama: Ensure Ollama is running at http://localhost:11434");
            
            throw new IllegalStateException(
                "No ChatModel bean found. " +
                "Please configure either:\n" +
                "1. Google GenAI: Set GEMINI_API_KEY environment variable (Google AI Studio - FREE, no billing)\n" +
                "   Get API key from: https://aistudio.google.com/app/apikey\n" +
                "2. Ollama: Ensure Ollama is running at http://localhost:11434\n" +
                "   Run: docker-compose up -d ollama (if using Docker) or start Ollama service"
            );
        }
        
        // Priority: Google GenAI (Gemini) > Ollama
        // Spring AI 1.1.0-RC1: Direct support for Google AI Studio API key
        // When spring.ai.google.genai.api-key is set, uses Gemini Developer API (no project-id needed)
        // Google GenAI will be first if GEMINI_API_KEY is set
        ChatModel model = chatModels.get(0);
        String modelName = model.getClass().getSimpleName();
        log.info("Configuring ChatClient with {} model (found {} ChatModel beans)", modelName, chatModels.size());
        
        if (chatModels.size() > 1) {
            log.info("Available models: {}", chatModels.stream()
                    .map(m -> m.getClass().getSimpleName())
                    .toList());
        }
        
        return ChatClient.builder(model)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

}
