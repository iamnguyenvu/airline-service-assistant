package io.github.nguyenvu.backend.policy.controller;

import io.github.nguyenvu.backend.policy.dto.AskRequest;
import io.github.nguyenvu.backend.policy.dto.AskWithFilterRequest;
import io.github.nguyenvu.backend.policy.dto.IngestRequest;
import io.github.nguyenvu.backend.policy.service.PolicyIngestionService;
import io.github.nguyenvu.backend.policy.service.PolicyQAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/policy")
public class PolicyController {
    private final PolicyIngestionService policyIngestionService;
    private final PolicyQAService policyQAService;

    @Operation(summary = "Ingest raw policy text into vector store")
    @ApiResponse(responseCode = "200", description = "Ingestion successful")
    @PostMapping("/ingest")
    public ResponseEntity<Void> ingest(@Valid @RequestBody IngestRequest ingestRequest) {
        policyIngestionService.ingestRawTest(ingestRequest.getText(), ingestRequest.getMetadata());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@Valid @RequestBody AskRequest askRequest) {
        String answer = policyQAService.ask(askRequest.getQuestion());
        return ResponseEntity.ok(Map.of("answer", answer));
    }
    @PostMapping("/ask/filter")
    public ResponseEntity<Map<String, String>> askWithFilter(@Valid @RequestBody AskWithFilterRequest askWithFilterRequest) {
        String answer = policyQAService.askWithFilter(
                askWithFilterRequest.getQuestion(),
                askWithFilterRequest.getAirlineCode(),
                askWithFilterRequest.getDocType(),
                askWithFilterRequest.getTopK()
        );
        return ResponseEntity.ok(Map.of("answer", answer));
    }

}
