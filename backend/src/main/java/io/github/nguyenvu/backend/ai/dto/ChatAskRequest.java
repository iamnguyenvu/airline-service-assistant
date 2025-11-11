package io.github.nguyenvu.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatAskRequest {
    @NotBlank
    private String message;
    private String sessionId;
    private String locale;
}
