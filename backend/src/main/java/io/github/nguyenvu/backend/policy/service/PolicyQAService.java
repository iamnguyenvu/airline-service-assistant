package io.github.nguyenvu.backend.policy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PolicyQAService {
    private final PolicyRetrieverService retriever;
    private final PolicyAnswerComposer composer;

    public String ask(String question) {
        requireQuestion(question);
        List<Document> results = retriever.retrieve(question);
        return composer.compose(question, results);
    }

    public String askWithFilter(String question, String airlineCode, String docType, int topK) {
        requireQuestion(question);
        List<Document> documents = retriever.retrieve(question, airlineCode, docType, topK);
        return composer.compose(question, documents);
    }

    // ===== HELPER METHODS =====
    private void requireQuestion(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question cannot be null or blank");
        }
    }
}
