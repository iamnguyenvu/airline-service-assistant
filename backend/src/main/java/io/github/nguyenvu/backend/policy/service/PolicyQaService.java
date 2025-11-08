package io.github.nguyenvu.backend.policy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyQaService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public String ask(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("question is required");
        }

        log.info("Policy QA question: {}", question);

        // Retrieve relevant chunks (limit in-memory to top 5 to reduce prompt size)
        List<Document> docs = vectorStore.similaritySearch(question);
        if (docs == null || docs.isEmpty()) {
            return "Xin lỗi, tôi chưa có đủ dữ liệu để trả lời câu hỏi này.";
        }

        var topDocs = docs.stream().limit(5).toList();
        StringBuilder context = new StringBuilder();
        for (Document d : topDocs) {
            context.append("- ").append(d.getContent()).append("\n");
        }

        String prompt = """
            Bạn là trợ lý chính sách của hãng hàng không. Chỉ trả lời dựa trên CONTEXT bên dưới.
            Nếu không tìm thấy thông tin phù hợp trong CONTEXT, hãy nói rằng bạn không biết.

            CÂU HỎI:
            %s

            CONTEXT:
            %s

            Yêu cầu:
            - Trả lời ngắn gọn bằng bullet points
            - Trích dẫn ngắn nguồn/điều khoản nếu có (ví dụ: theo mục/điều)
            """.formatted(question, context);

        return chatClient
            .prompt()
            .user(prompt)
            .call()
            .content();
    }
}

