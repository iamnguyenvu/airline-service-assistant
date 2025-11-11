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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import io.github.nguyenvu.backend.policy.entity.ServiceDocs;

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
    
    @Operation(summary = "Upload and ingest policy document (PDF/TXT)")
    @ApiResponse(responseCode = "200", description = "Document uploaded and processed successfully")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("airlineCode") String airlineCode,
            @RequestParam(value = "docType", defaultValue = "policy") String docType) {
        
        String result = policyIngestionService.ingestDocument(file, airlineCode, docType);
        return ResponseEntity.ok(Map.of("message", result));
    }
    
    @Operation(summary = "List all uploaded documents")
    @ApiResponse(responseCode = "200", description = "Documents retrieved successfully")
    @GetMapping("/documents")
    public ResponseEntity<List<ServiceDocs>> listDocuments() {
        List<ServiceDocs> documents = policyIngestionService.listDocuments();
        return ResponseEntity.ok(documents);
    }
    
    @Operation(summary = "Delete a document by ID")
    @ApiResponse(responseCode = "200", description = "Document deleted successfully")
    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Map<String, String>> deleteDocument(@PathVariable Long id) {
        policyIngestionService.deleteDocument(id);
        return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));
    }

}
