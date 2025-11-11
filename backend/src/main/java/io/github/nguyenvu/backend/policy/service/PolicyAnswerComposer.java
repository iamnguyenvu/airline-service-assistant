package io.github.nguyenvu.backend.policy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PolicyAnswerComposer {
    private static final int MAX_CONTEXT_DOCS = 5;
    private static final int MAX_DOC_CHARS = 1200;

    private final ChatClient chatClient;

    public String compose(String question, List<Document> rawDocs) {
        if (rawDocs == null || rawDocs.isEmpty()) {
            return "Xin lỗi, tôi chưa có đủ dữ liệu để trả lời câu hỏi này.";
        }
        List<Document> dedupe = dedupeByMeta(rawDocs);
        List<Document> topDocs = dedupe.stream()
                .limit(MAX_CONTEXT_DOCS)
                .map(this::truncateDocument)
                .toList();

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < topDocs.size(); i++) {
            Document doc = topDocs.get(i);
            context.append("### DOC ").append(i + 1).append("\n");
            context.append(doc.getFormattedContent()).append("\n");
            context.append(cite(doc.getMetadata())).append("\n\n");
        }

        String system = """
            Bạn là trợ lý chính sách hãng hàng không.
            Chỉ trả lời dựa trên CONTEXT. Nếu không chắc chắn, hãy nói "Tôi không có thông tin trong dữ liệu hiện có."
            Trả lời súc tích bằng bullet points; nêu mục/điều nếu thấy trong CONTEXT.
            """;
        String user = """
            CÂU HỎI:
            %s

            CONTEXT:
            %s
            """.formatted(question.trim(), context.toString().trim());

        return chatClient.prompt()
                .system(system)
                .user(user)
                .call()
                .content();
    }

    private List<Document> dedupeByMeta(List<Document> docs) {
        Set<String> seen = new HashSet<>();
        List<Document> out = new ArrayList<>();
        for (Document doc : docs) {
            Map<String, Object> m = doc.getMetadata();
            String key = sv(m.get("source")) + "|" + sv(m.get("page")) + "|" + sv(m.get("section"));
            if (key.isBlank()) key = Integer.toHexString(Objects.hashCode(doc.getFormattedContent()));
            if (seen.add(key)) out.add(doc);
        }
        return out;
    }

    private String sv(Object object) {
        return object == null ? "" : String.valueOf(object);
    }

    private Document truncateDocument(Document document) {
        String content = document.getText();
        if (content == null) content = "";
        if (content.length() > MAX_DOC_CHARS) content = content.substring(0, MAX_DOC_CHARS) + " ...";
        return new Document(content, document.getMetadata());
    }

    private String cite(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return "_[nguồn: N/A]_";
        String airline = sv(metadata.get("airline_code"));
        String type = sv(metadata.get("doc_type"));
        String src = sv(metadata.get("source"));
        String page = sv(metadata.get("page"));
        String ver = sv(metadata.get("version"));
        String date = sv(metadata.get("date"));
        List<String> parts = new ArrayList<>();
        if (!airline.isBlank()) parts.add("airline " + airline);
        if (!type.isBlank()) parts.add("doc " + type);
        if (!page.isBlank()) parts.add("page " + page);
        if (!ver.isBlank()) parts.add("v" + ver);
        if (!date.isBlank()) parts.add(date);
        if (!src.isBlank()) parts.add(src);
        return parts.isEmpty() ? "_[nguồn: n/a]_" : "_[" + String.join(", ", parts) + "]_";
    }
}


