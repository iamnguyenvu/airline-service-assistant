package io.github.nguyenvu.backend.policy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyIngestionService {

    private final VectorStore vectorStore;

    /**
     * Ingest a raw policy text into the vector store with metadata.
     * Metadata keys can include: airline_code, doc_type, source_url, version_tag
     */
    public void ingest(String rawText, Map<String, Object> metadata) {
        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("rawText is required");
        }

        var meta = metadata != null ? metadata : Map.of();
        log.info("Ingesting policy text (len={}) with meta={}", rawText.length(), meta);

        // Split into chunks for better retrieval
        var splitter = new TokenTextSplitter(800, 200);
        List<Document> chunks = splitter.apply(List.of(new Document(rawText, meta)));

        vectorStore.add(chunks);
        log.info("Ingested {} chunks into vector store", chunks.size());
    }
}

