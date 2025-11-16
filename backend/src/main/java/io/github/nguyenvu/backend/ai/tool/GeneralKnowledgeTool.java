package io.github.nguyenvu.backend.ai.tool;

import io.github.nguyenvu.backend.policy.service.PolicyQAService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * Tool for retrieving general knowledge and FAQ information from the vector store.
 * This helps the chatbot answer general questions not specifically about flights or policies.
 */
@Slf4j
@Component("generalKnowledge")
@RequiredArgsConstructor
public class GeneralKnowledgeTool implements Function<GeneralKnowledgeTool.Request, String> {

    private final VectorStore vectorStore;
    private static final int DEFAULT_TOPK = 5;
    private static final double DEFAULT_SIM_THRESHOLD = 0.40; // Lower threshold for general knowledge

    @Data
    public static class Request {
        private String query;
        private Integer topK;
    }

    @Override
    public String apply(Request req) {
        log.info("[Tool] generalKnowledge query: {}", req.getQuery());
        
        if (req.getQuery() == null || req.getQuery().isBlank()) {
            return "Xin lỗi, tôi cần câu hỏi cụ thể để tìm kiếm thông tin.";
        }
        
        try {
            // Search in vector store for general knowledge
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(req.getQuery())
                    .topK(req.getTopK() != null && req.getTopK() > 0 ? req.getTopK() : DEFAULT_TOPK)
                    .similarityThreshold(DEFAULT_SIM_THRESHOLD)
                    .build();
            
            List<Document> results = vectorStore.similaritySearch(searchRequest);
            
            if (results == null || results.isEmpty()) {
                return "Tôi không tìm thấy thông tin liên quan trong cơ sở dữ liệu. " +
                       "Bạn có thể hỏi về chuyến bay, chính sách hành lý, đổi vé, hoàn vé, hoặc các dịch vụ khác của hãng hàng không.";
            }
            
            // Build context from retrieved documents
            StringBuilder context = new StringBuilder();
            for (int i = 0; i < results.size(); i++) {
                Document doc = results.get(i);
                context.append("### Thông tin ").append(i + 1).append("\n");
                context.append(doc.getFormattedContent()).append("\n");
                
                // Add metadata citation if available
                if (doc.getMetadata() != null && !doc.getMetadata().isEmpty()) {
                    String source = doc.getMetadata().getOrDefault("source", "").toString();
                    String docType = doc.getMetadata().getOrDefault("doc_type", "").toString();
                    if (!source.isEmpty() || !docType.isEmpty()) {
                        context.append("_Nguồn: ").append(docType.isEmpty() ? source : docType).append("_\n");
                    }
                }
                context.append("\n");
            }
            
            return context.toString().trim();
            
        } catch (Exception e) {
            log.error("Error in generalKnowledge tool: {}", e.getMessage(), e);
            return "Xin lỗi, đã xảy ra lỗi khi tìm kiếm thông tin. Vui lòng thử lại.";
        }
    }
}

