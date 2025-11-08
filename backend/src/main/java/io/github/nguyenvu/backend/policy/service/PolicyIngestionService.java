package io.github.nguyenvu.backend.policy.service;

import io.github.nguyenvu.backend.policy.util.ChunkPostProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyIngestionService {
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

        var metadata = meta != null ? meta : Map.of();
        var doc = new Document(normalizedText, (Map<String, Object>) metadata);

        // Split document into chunks
        List<Document> chunks = splitter.apply(List.of(doc));
        ChunkPostProcessor chunkPostProcessor = new ChunkPostProcessor(22, 2);

        Predicate<String> garbage = line -> line.trim()
                .matches("(?i)^(confidential|watermark|footer.*|header.*)$");

        List<Document> cleaned = chunkPostProcessor.mergeAndFilter(chunks, garbage);

        // Ingest chunks into vector store
        vectorStore.add(chunks);
        log.info("Ingested {} chunks into vector store, metadata={}", chunks.size(), metadata);
    }
}
