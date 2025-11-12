package io.github.nguyenvu.backend.policy.service;

import io.github.nguyenvu.backend.policy.entity.ServiceDocs;
import io.github.nguyenvu.backend.policy.repository.ServiceDocsRepository;
import io.github.nguyenvu.backend.policy.util.ChunkPostProcessor;
import io.github.nguyenvu.backend.policy.util.ContextMatcher;
import io.github.nguyenvu.backend.policy.util.DefaultContextMatcher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.apache.tika.Tika;
import org.springframework.core.io.InputStreamResource;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class PolicyIngestionService {
    private static final Logger log = LoggerFactory.getLogger(PolicyIngestionService.class);
    
    private final VectorStore vectorStore;
    private final ServiceDocsRepository serviceDocsRepository;
    private final TokenTextSplitter splitter = new TokenTextSplitter();

    public void ingestRawTest(String rawText, Map<String, Object> meta) {
        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("Raw text cannot be null or blank");
        }
        // Normalize text
        String normalizedText = rawText
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\u00A0", " ")
                .trim();

        Map<String, Object> metadata = meta != null ? meta : Map.of();
        var doc = new Document(normalizedText, metadata);

        // Split document into chunks
        List<Document> chunks = splitter.apply(List.of(doc));
        ContextMatcher contextMatcher = new DefaultContextMatcher(List.of("page", "section"));
        ChunkPostProcessor chunkPostProcessor = new ChunkPostProcessor(22, 2, contextMatcher);
        

        Predicate<String> garbage = line -> line.trim()
                .matches("(?i)^(confidential|watermark|footer.*|header.*)$");

        List<Document> cleaned = chunkPostProcessor.mergeAndFilter(chunks, garbage);

        // Ingest chunks into vector store
        vectorStore.add(cleaned);
        log.info("Ingested {} chunks into vector store, metadata={}", chunks.size(), metadata);
    }
    
    @Transactional
    public String ingestDocument(MultipartFile file, String airlineCode, String docType) {
        try {
            log.info("Starting document ingestion: {} for airline: {}", file.getOriginalFilename(), airlineCode);
            
            // 1. Save document metadata to database
            ServiceDocs serviceDoc = ServiceDocs.builder()
                    .airlineCode(airlineCode.toUpperCase())
                    .docType(docType)
                    .rawText("") // Will be populated after processing
                    .sourceUrl(file.getOriginalFilename())
                    .versionTag("1.0")
                    .build();
            
            serviceDoc = serviceDocsRepository.save(serviceDoc);
            String docId = serviceDoc.getId().toString();
            
            // 2. Extract text from uploaded file
            String extractedText = extractTextFromFile(file);
            
            // 3. Update document with extracted text
            serviceDoc.setRawText(extractedText);
            serviceDocsRepository.save(serviceDoc);
            
            // 4. Use existing ingestRawTest method with metadata
            Map<String, Object> metadata = Map.of(
                "source", file.getOriginalFilename(),
                "airline_code", airlineCode,
                "doc_type", docType,
                "doc_id", docId,
                "ingested_at", LocalDateTime.now().toString()
            );
            
            ingestRawTest(extractedText, metadata);
            
            log.info("Successfully ingested document: {}", file.getOriginalFilename());
            return String.format("Successfully processed document %s for %s", 
                    file.getOriginalFilename(), airlineCode);
            
        } catch (Exception e) {
            log.error("Error during document ingestion: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to ingest document: " + e.getMessage(), e);
        }
    }
    
    private String extractTextFromFile(MultipartFile file) throws IOException {
        try {
            Tika tika = new Tika();
            String extractedText = tika.parseToString(file.getInputStream());
            log.info("Successfully extracted text from file: {} ({} characters)", 
                     file.getOriginalFilename(), extractedText.length());
            return extractedText;
        } catch (Exception e) {
            log.warn("Failed to extract text with Tika for file: {}, error: {}", 
                     file.getOriginalFilename(), e.getMessage());
            // Fallback: treat as plain text
            return new String(file.getBytes());
        }
    }
    
    public List<ServiceDocs> listDocuments() {
        return serviceDocsRepository.findAll();
    }
    
    @Transactional
    public void deleteDocument(Long docId) {
        log.info("Deleting document with ID: {}", docId);

        serviceDocsRepository.deleteById(docId);
        log.info("Document {} deleted successfully", docId);
    }
}
