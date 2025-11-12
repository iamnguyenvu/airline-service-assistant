package io.github.nguyenvu.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ChatAskRequest {
    @NotBlank
    private String message;
    private String sessionId;
    private String locale;
    private List<ChatMessage> conversationHistory;
    
    @Data
    public static class ChatMessage {
        private String role; // "user" or "assistant"
        private String content;
    }
}
