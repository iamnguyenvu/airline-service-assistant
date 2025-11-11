package io.github.nguyenvu.backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAskResponse {
    private String answer;
    private boolean usedTools;
    private String model;
    private String sessionId;
}
