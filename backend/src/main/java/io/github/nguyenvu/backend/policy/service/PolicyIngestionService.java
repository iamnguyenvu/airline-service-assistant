package io.github.nguyenvu.backend.policy.service;

import io.github.nguyenvu.backend.policy.util.ChunkPostProcessor;
import io.github.nguyenvu.backend.policy.util.ContextMatcher;
import io.github.nguyenvu.backend.policy.util.DefaultContextMatcher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class PolicyIngestionService {
    private static final Logger log = LoggerFactory.getLogger(PolicyIngestionService.class);
    
    private final VectorStore vectorStore;
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
}
