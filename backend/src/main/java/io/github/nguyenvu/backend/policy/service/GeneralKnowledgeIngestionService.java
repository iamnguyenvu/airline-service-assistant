package io.github.nguyenvu.backend.policy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for ingesting general knowledge and FAQ data into the vector store.
 * This helps the chatbot answer general questions about airlines, airports, travel, etc.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralKnowledgeIngestionService {

    private final VectorStore vectorStore;
    private final PolicyIngestionService policyIngestionService;

    /**
     * Ingest a general knowledge document or FAQ entry
     */
    public void ingestGeneralKnowledge(String content, String category, String title) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or blank");
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("doc_type", "general_knowledge");
        metadata.put("category", category != null ? category : "general");
        metadata.put("title", title != null ? title : "General Information");
        metadata.put("source", "manual_ingestion");
        metadata.put("ingested_at", java.time.LocalDateTime.now().toString());

        policyIngestionService.ingestRawTest(content, metadata);
        log.info("Ingested general knowledge: category={}, title={}", category, title);
    }

    /**
     * Ingest multiple FAQ entries at once
     */
    public void ingestFAQs(List<FAQEntry> faqs) {
        for (FAQEntry faq : faqs) {
            String content = "Câu hỏi: " + faq.question + "\n\nTrả lời: " + faq.answer;
            ingestGeneralKnowledge(content, "faq", faq.question);
        }
        log.info("Ingested {} FAQ entries", faqs.size());
    }

    /**
     * Initialize with some common general knowledge about airlines and airports
     */
    public void initializeCommonKnowledge() {
        log.info("Initializing common general knowledge...");

        // Check-in information
        ingestGeneralKnowledge(
            "Thủ tục check-in tại sân bay:\n" +
            "- Check-in online: Thường mở từ 24-48 giờ trước giờ bay, đóng 1-2 giờ trước giờ bay\n" +
            "- Check-in tại sân bay: Thường mở 2-3 giờ trước giờ bay, đóng 45-60 phút trước giờ bay\n" +
            "- Cần mang theo: Giấy tờ tùy thân (CMND/CCCD/Hộ chiếu), vé máy bay (hoặc mã đặt chỗ)\n" +
            "- Hành lý: Kiểm tra trọng lượng và kích thước hành lý xách tay trước khi check-in\n" +
            "- Sau khi check-in: Nhận thẻ lên máy bay, kiểm tra cổng và giờ bay",
            "check_in",
            "Thủ tục check-in tại sân bay"
        );

        // Airport information
        ingestGeneralKnowledge(
            "Thông tin sân bay tại Việt Nam:\n" +
            "- Sân bay Tân Sơn Nhất (SGN): Sân bay quốc tế lớn nhất tại TP.HCM, phục vụ cả nội địa và quốc tế\n" +
            "- Sân bay Nội Bài (HAN): Sân bay quốc tế chính tại Hà Nội, phục vụ cả nội địa và quốc tế\n" +
            "- Sân bay Đà Nẵng (DAD): Sân bay quốc tế tại miền Trung\n" +
            "- Sân bay Phú Quốc (PQC): Sân bay quốc tế tại đảo Phú Quốc\n" +
            "- Các sân bay khác: Cần Thơ (VCA), Vinh (VII), Cát Bi Hải Phòng (HPH), Cam Ranh (CXR), v.v.\n" +
            "- Dịch vụ tại sân bay: Quầy check-in, cửa hàng miễn thuế, nhà hàng, phòng chờ, dịch vụ đỗ xe",
            "airport",
            "Thông tin sân bay Việt Nam"
        );

        // Baggage information
        ingestGeneralKnowledge(
            "Hành lý khi đi máy bay:\n" +
            "- Hành lý xách tay: Thường cho phép 7-10kg, kích thước tối đa 56x36x23cm (tùy hãng)\n" +
            "- Hành lý ký gửi: Trọng lượng và số lượng tùy theo hạng vé và hãng hàng không\n" +
            "- Hành cấm: Chất lỏng quá 100ml, vũ khí, chất nổ, chất độc, v.v.\n" +
            "- Hành lý đặc biệt: Thể thao, nhạc cụ, động vật cần thông báo trước\n" +
            "- Quá cước hành lý: Phí quá cước thường tính theo kg, giá tùy hãng và tuyến bay",
            "baggage",
            "Thông tin hành lý"
        );

        // Flight booking
        ingestGeneralKnowledge(
            "Đặt vé máy bay:\n" +
            "- Có thể đặt vé qua website, ứng dụng di động, hoặc đại lý du lịch\n" +
            "- Thông tin cần có: Họ tên (đúng như giấy tờ), ngày sinh, số CMND/CCCD/Hộ chiếu\n" +
            "- Thanh toán: Thẻ tín dụng, thẻ ghi nợ, ví điện tử, chuyển khoản\n" +
            "- Xác nhận: Sau khi đặt thành công, sẽ nhận được email xác nhận với mã đặt chỗ (PNR)\n" +
            "- Kiểm tra vé: Có thể tra cứu bằng mã đặt chỗ hoặc số điện thoại/email đã đăng ký",
            "booking",
            "Hướng dẫn đặt vé máy bay"
        );

        // Travel tips
        ingestGeneralKnowledge(
            "Mẹo du lịch bằng máy bay:\n" +
            "- Đến sân bay sớm: Nên đến 2-3 giờ trước giờ bay quốc tế, 1-2 giờ trước giờ bay nội địa\n" +
            "- Chuẩn bị giấy tờ: Luôn mang theo giấy tờ tùy thân gốc, không mang bản sao\n" +
            "- Hành lý: Đóng gói cẩn thận, dán nhãn tên và số điện thoại\n" +
            "- Sức khỏe: Uống đủ nước, tránh uống rượu bia trước khi bay\n" +
            "- Thời tiết: Kiểm tra thời tiết tại điểm đến, chuẩn bị quần áo phù hợp\n" +
            "- Bảo hiểm: Nên mua bảo hiểm du lịch để được bảo vệ trong trường hợp rủi ro",
            "travel_tips",
            "Mẹo du lịch bằng máy bay"
        );

        log.info("Common general knowledge initialized");
    }

    /**
     * Simple FAQ entry class
     */
    public static class FAQEntry {
        public String question;
        public String answer;

        public FAQEntry(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }
}



