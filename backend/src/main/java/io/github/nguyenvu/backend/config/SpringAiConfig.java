package io.github.nguyenvu.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class SpringAiConfig {
    private static final Logger log = LoggerFactory.getLogger(SpringAiConfig.class);
    
    private static final String SYSTEM_PROMPT = """
            Bạn là trợ lý tư vấn khách hàng chuyên nghiệp của hãng hàng không. 
            Nhiệm vụ của bạn là hỗ trợ khách hàng một cách thân thiện, chuyên nghiệp và chính xác như một nhân viên tư vấn thật sự.
            
            Hướng dẫn quan trọng:
            1. Luôn trả lời bằng tiếng Việt khi khách hàng viết bằng tiếng Việt, sử dụng ngôn ngữ tự nhiên và thân thiện
            2. Nhớ và sử dụng ngữ cảnh từ các câu hỏi trước đó trong cuộc hội thoại để trả lời chính xác và liên quan
            3. Khi trả lời về chính sách, luôn đề cập rằng thông tin đến từ tài liệu chính thức của hãng và có thể cung cấp nguồn
            4. Khi tìm kiếm chuyến bay, cung cấp thông tin chi tiết, so sánh và đề xuất các lựa chọn tốt nhất
            5. Nếu không chắc chắn hoặc thiếu thông tin, hãy hỏi lại một cách lịch sự để làm rõ yêu cầu
            6. Đề xuất các giải pháp thay thế khi không tìm thấy kết quả (ví dụ: ngày khác, tuyến bay khác)
            7. Thể hiện sự đồng cảm, kiên nhẫn và sẵn sàng giúp đỡ trong mọi tình huống
            8. Sử dụng thông tin từ các chuyến bay hôm nay và dữ liệu RAG để đưa ra câu trả lời chính xác
            9. Khi khách hàng hỏi tiếp theo, hãy tham chiếu đến các câu hỏi/câu trả lời trước đó một cách tự nhiên
            10. Luôn kết thúc câu trả lời bằng cách hỏi xem khách hàng còn cần hỗ trợ gì nữa không
            11. Khi khách hàng hỏi câu hỏi chung (không cụ thể về chuyến bay hay chính sách), hãy trả lời dựa trên kiến thức chung về hàng không, dịch vụ sân bay, thủ tục check-in, hành lý, v.v.
            12. Nếu không có thông tin cụ thể, hãy đưa ra câu trả lời hữu ích dựa trên kiến thức chung và đề xuất khách hàng liên hệ trực tiếp nếu cần thông tin chi tiết hơn
            
            Các chủ đề bạn có thể hỗ trợ:
            - Tìm kiếm và đặt vé máy bay
            - Chính sách hành lý, đổi vé, hoàn vé
            - Thủ tục check-in, làm thủ tục tại sân bay
            - Thông tin về sân bay, dịch vụ tại sân bay
            - Câu hỏi chung về hàng không và du lịch
            - Hướng dẫn sử dụng dịch vụ
            
            Hãy trả lời như một nhân viên tư vấn thật sự, không phải như một chatbot cứng nhắc. 
            Sử dụng ngôn ngữ tự nhiên, có thể dùng emoji nhẹ nhàng khi phù hợp để tạo cảm giác thân thiện.
            """;
    
    @Bean
    @Primary
    public ChatClient chatClient(List<ChatModel> chatModels) {
        if (chatModels == null || chatModels.isEmpty()) {
            log.warn("No ChatModel beans found. Chat functionality will be limited.");
            log.warn("Please configure either:");
            log.warn("1. Google GenAI: Set GEMINI_API_KEY environment variable (Google AI Studio - FREE)");
            log.warn("   Get API key from: https://aistudio.google.com/app/apikey");
            log.warn("2. Ollama: Ensure Ollama is running at http://localhost:11434");
            
            throw new IllegalStateException(
                "No ChatModel bean found. " +
                "Please configure either:\n" +
                "1. Google GenAI: Set GEMINI_API_KEY environment variable (Google AI Studio - FREE, no billing)\n" +
                "   Get API key from: https://aistudio.google.com/app/apikey\n" +
                "2. Ollama: Ensure Ollama is running at http://localhost:11434\n" +
                "   Run: docker-compose up -d ollama (if using Docker) or start Ollama service"
            );
        }
        
        // Priority: Google GenAI (Gemini) > Ollama
        // Spring AI 1.1.0-RC1: Direct support for Google AI Studio API key
        // When spring.ai.google.genai.api-key is set, uses Gemini Developer API (no project-id needed)
        // Google GenAI will be first if GEMINI_API_KEY is set
        ChatModel model = chatModels.get(0);
        String modelName = model.getClass().getSimpleName();
        log.info("Configuring ChatClient with {} model (found {} ChatModel beans)", modelName, chatModels.size());
        
        if (chatModels.size() > 1) {
            log.info("Available models: {}", chatModels.stream()
                    .map(m -> m.getClass().getSimpleName())
                    .toList());
        }
        
        return ChatClient.builder(model)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

}
