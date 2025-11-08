package io.github.nguyenvu.backend.policy.controller;

import io.github.nguyenvu.backend.policy.service.PolicyIngestionService;
import io.github.nguyenvu.backend.policy.service.PolicyQaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
@Slf4j
public class PolicyController {

    private final PolicyIngestionService ingestionService;
    private final PolicyQaService qaService;

    @PostMapping("/ingest")
    public ResponseEntity<Void> ingest(@RequestBody Map<String, Object> body) {
        String text = (String) body.getOrDefault("text", "");
        @SuppressWarnings("unchecked")
        Map<String, Object> meta = (Map<String, Object>) body.getOrDefault("meta", Map.of());

        ingestionService.ingest(text, meta);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@RequestBody Map<String, String> body) {
        String question = body.getOrDefault("question", "");
        String answer = qaService.ask(question);
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}

