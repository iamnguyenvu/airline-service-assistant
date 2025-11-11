package io.github.nguyenvu.backend.ai.service;

import io.github.nguyenvu.backend.ai.dto.ChatAskRequest;
import io.github.nguyenvu.backend.ai.dto.ChatAskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiOrchestratorService {

    private final ChatClient chatClient;

    public ChatAskResponse ask(ChatAskRequest req) {
        var result = chatClient
                .prompt()
                .user(req.getMessage())
                .call();

        String answer = result.content();
        String model = null;
        boolean usedTools = false;

        return ChatAskResponse.builder()
                .answer(answer)
                .usedTools(usedTools)
                .model(model)
                .sessionId(req.getSessionId())
                .build();
    }
}
