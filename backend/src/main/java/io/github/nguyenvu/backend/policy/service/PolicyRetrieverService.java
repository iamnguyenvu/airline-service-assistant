package io.github.nguyenvu.backend.policy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyRetrieverService {
    private static final int DEFAULT_TOPK = 8;
    private static final double DEFAULT_SIM_THRESHOLD = 0.55;

    private final VectorStore vectorStore;

    public List<Document> retrieve(String question) {
        return retrieve(question, null, null, DEFAULT_TOPK);
    }

    public List<Document> retrieve(String question, String airlineCode, String docType, int topK) {
        String filterExp = buildFilterExpression(airlineCode, docType);
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(question)
                .topK(topK > 0 ? topK : DEFAULT_TOPK)
                .similarityThreshold(DEFAULT_SIM_THRESHOLD);
        if (!filterExp.isEmpty()) {
            builder.filterExpression(filterExp);
        }
        return vectorStore.similaritySearch(builder.build());
    }

    String buildFilterExpression(String airlineCode, String docType) {
        List<String> conditions = new ArrayList<>();
        if (airlineCode != null && !airlineCode.isBlank()) {
            conditions.add("airline_code == '" + esc(airlineCode) + "'");
        }
        if (docType != null && !docType.isBlank()) {
            conditions.add("doc_type == '" + esc(docType) + "'");
        }
        return String.join(" && ", conditions);
    }

    private String esc(String str) {
        return str.replace("'", "\\'");
    }
}


