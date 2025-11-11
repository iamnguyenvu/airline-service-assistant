package io.github.nguyenvu.backend.ai.controller;

import io.github.nguyenvu.backend.ai.dto.ChatAskRequest;
import io.github.nguyenvu.backend.ai.dto.ChatAskResponse;
import io.github.nguyenvu.backend.ai.service.AiOrchestratorService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AiOrchestratorService orchestrator;

    @PostMapping("/ask")
    @Operation(summary = "Hỏi trợ lý AI (tool-first; có citation khi đụng policy)")
    public ResponseEntity<ChatAskResponse> ask(@Valid @RequestBody ChatAskRequest request) {
        return ResponseEntity.ok(orchestrator.ask(request));
    }
}
