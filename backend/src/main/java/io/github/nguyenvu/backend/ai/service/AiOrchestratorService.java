package io.github.nguyenvu.backend.ai.service;

import io.github.nguyenvu.backend.ai.dto.ChatAskRequest;
import io.github.nguyenvu.backend.ai.dto.ChatAskResponse;
import io.github.nguyenvu.backend.ai.entity.ConversationMessage;
import io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria;
import io.github.nguyenvu.backend.flight.dto.FlightSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import io.github.nguyenvu.backend.ai.tool.EstimateCO2Tool;
import io.github.nguyenvu.backend.ai.tool.GeneralKnowledgeTool;
import io.github.nguyenvu.backend.ai.tool.LiveStatusTool;
import io.github.nguyenvu.backend.ai.tool.RagPolicyTool;
import io.github.nguyenvu.backend.ai.tool.SearchFlightsTool;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiOrchestratorService {

    private final ChatClient chatClient;
    private final io.github.nguyenvu.backend.flight.service.FlightSearchService flightSearchService;
    private final io.github.nguyenvu.backend.policy.service.PolicyQAService policyQAService;
    private final SearchFlightsTool searchFlightsTool;
    private final RagPolicyTool ragPolicyTool;
    private final GeneralKnowledgeTool generalKnowledgeTool;
    private final EstimateCO2Tool estimateCO2Tool;
    private final LiveStatusTool liveStatusTool;
    private final ConversationService conversationService;
    private final ObjectMapper objectMapper;

    private static final int HISTORY_LIMIT = 20;
    private static final int CONTEXT_CHAR_BUDGET = 3000;

    public ChatAskResponse ask(ChatAskRequest req) {
        // Get or create conversation
        String sessionId = req.getSessionId() != null ? req.getSessionId() : "session-" + System.currentTimeMillis();
        
        // Prefer calling domain tools/services directly when intent is clear.
        // If not clear, fall back to LLM for guidance.
        String message = req.getMessage() != null ? req.getMessage().trim() : "";
        if (message.isBlank()) {
            return ChatAskResponse.builder()
                    .answer("Bạn hãy nhập câu hỏi hoặc yêu cầu cụ thể nhé.")
                    .usedTools(false)
                    .sessionId(sessionId)
                    .build();
        }
        var conversation = conversationService.getOrCreateConversation(sessionId);
        
        // Save user message
        conversationService.saveUserMessage(conversation, message);
        
        // Get conversation history (last 10 messages for context)
        List<ConversationMessage> history = 
            conversationService.getConversationHistory(sessionId, HISTORY_LIMIT);
        
        // Build conversation summary
        String summary = buildConversationSummary(history);
        
        // Update conversation title from first message if not set
        if (conversation.getTitle() == null || conversation.getTitle().equals("New Conversation")) {
            String title = message.length() > 50 ? message.substring(0, 50) + "..." : message;
            conversationService.updateConversationTitle(conversation, title);
        }

        // Runtime tool-first: explicit tool call "tool.<name> {json}"
        ChatAskResponse runtimeTool = tryHandleRuntimeToolCall(message, sessionId);
        if (runtimeTool != null) {
            return runtimeTool;
        }

        if (looksLikePolicyQuestion(message)) {
            // Tool-first: route to policy retriever+composer
            String answer = policyQAService.ask(message);
            // Save assistant message
            conversationService.saveAssistantMessage(conversation, answer, true, null);
            
            return ChatAskResponse.builder()
                    .answer(answer)
                    .usedTools(true)
                    .sessionId(sessionId)
                    .build();
        }

        if (looksLikeFlightSearch(message)) {
            // Tool-first: route to flight search service
            try {
                var criteria = parseBasicCriteria(message);
                var result = flightSearchService.search(criteria, 0, 10);
                
                String answer;
                if (result.getTotalElements() == 0 || result.getFlights().isEmpty()) {
                    // No flights found - provide helpful message
                    if (criteria.getDepIata() != null && criteria.getArrIata() != null) {
                        answer = "Không tìm thấy chuyến bay nào từ " + criteria.getDepIata() + 
                                " đến " + criteria.getArrIata() + 
                                " vào ngày " + (criteria.getSnapshotDate() != null ? criteria.getSnapshotDate() : "đã chọn") + 
                                ". Vui lòng thử lại với ngày khác hoặc tuyến bay khác.";
                    } else if (criteria.getArrIata() != null) {
                        answer = "Không tìm thấy chuyến bay nào đến " + criteria.getArrIata() + 
                                " vào ngày " + (criteria.getSnapshotDate() != null ? criteria.getSnapshotDate() : "đã chọn") + 
                                ". Vui lòng thử lại với ngày khác.";
                    } else if (criteria.getDepIata() != null) {
                        answer = "Không tìm thấy chuyến bay nào từ " + criteria.getDepIata() + 
                                " vào ngày " + (criteria.getSnapshotDate() != null ? criteria.getSnapshotDate() : "đã chọn") + 
                                ". Vui lòng thử lại với ngày khác.";
                    } else {
                        answer = "Không tìm thấy chuyến bay phù hợp. Vui lòng cung cấp thông tin chi tiết hơn (sân bay đi, sân bay đến, ngày bay).";
                    }
                } else {
                    answer = "Tìm thấy " + result.getTotalElements() + 
                            " chuyến phù hợp. Ví dụ: " + formatFlightSample(result);
                }
                
                // Save assistant message
                conversationService.saveAssistantMessage(conversation, answer, true, null);
                
                return ChatAskResponse.builder()
                        .answer(answer)
                        .usedTools(true)
                        .sessionId(sessionId)
                        .flightResults(result)
                        .build();
            } catch (IllegalArgumentException e) {
                // Invalid criteria - return helpful error message
                log.warn("Invalid flight search criteria: {}", e.getMessage());
                return ChatAskResponse.builder()
                        .answer("Xin lỗi, tôi không thể tìm chuyến bay với thông tin bạn cung cấp. " +
                                "Vui lòng cung cấp đầy đủ thông tin: sân bay đi, sân bay đến, và ngày bay. " +
                                "Ví dụ: 'danh sách chuyến bay ngày 12 tháng 11 năm 2025 từ Sài gòn đến Hà nội'")
                        .usedTools(false)
                        .sessionId(sessionId)
                        .build();
            } catch (Exception e) {
                log.error("Error searching flights: {}", e.getMessage(), e);
                return ChatAskResponse.builder()
                        .answer("Xin lỗi, đã xảy ra lỗi khi tìm kiếm chuyến bay. Vui lòng thử lại sau.")
                        .usedTools(false)
                        .sessionId(sessionId)
                        .build();
            }
        }

        // Try general knowledge RAG first for general queries
        if (looksLikeGeneralQuestion(message)) {
            try {
                log.info("Attempting general knowledge RAG for query: {}", message);
                GeneralKnowledgeTool.Request genReq = new GeneralKnowledgeTool.Request();
                genReq.setQuery(message);
                genReq.setTopK(5);
                String genKnowledge = generalKnowledgeTool.apply(genReq);
                
                if (genKnowledge != null && !genKnowledge.contains("không tìm thấy") 
                    && !genKnowledge.contains("không có thông tin")) {
                    // Found relevant information, compose answer with LLM
                    String composedAnswer = composeAnswerWithContext(message, genKnowledge, history);
                    
                    conversationService.saveAssistantMessage(conversation, composedAnswer, true, null);
                    
                    return ChatAskResponse.builder()
                            .answer(composedAnswer)
                            .usedTools(true)
                            .model("gemini-2.5-flash")
                            .sessionId(sessionId)
                            .build();
                }
            } catch (Exception e) {
                log.warn("General knowledge RAG failed, falling back to LLM: {}", e.getMessage());
            }
        }

        // Fallback: Use LLM with automatic tool calling and conversation history
        try {
            // Build conversation context from history
            StringBuilder contextBuilder = new StringBuilder();
            if (!history.isEmpty()) {
                List<ChatAskRequest.ChatMessage> chatHistory = conversationService.toChatMessages(history);
                contextBuilder.append("Ngữ cảnh cuộc hội thoại trước đó:\n\n");
                for (ChatAskRequest.ChatMessage histMsg : chatHistory) {
                    if ("user".equals(histMsg.getRole())) {
                        contextBuilder.append("Khách hàng: ").append(histMsg.getContent()).append("\n\n");
                    } else if ("assistant".equals(histMsg.getRole())) {
                        contextBuilder.append("Trợ lý: ").append(histMsg.getContent()).append("\n\n");
                    }
                }
                contextBuilder.append("---\n\n");
            }
            
            // Build final user message with context
            String userMessageWithContext = contextBuilder.toString() + 
                "Câu hỏi hiện tại của khách hàng: " + message;

            var result = chatClient
                    .prompt()
                    .user(userMessageWithContext)
                    .call()
                    .content();

            boolean usedTools = (result != null && result.contains("[Tool]")) ||
                    message.toLowerCase().contains("tool.");
            
            // Save assistant message
            conversationService.saveAssistantMessage(conversation, result, usedTools, null);

            return ChatAskResponse.builder()
                    .answer(result != null ? result : "Xin lỗi, tôi không thể xử lý yêu cầu này.")
                    .usedTools(usedTools)
                    .model("gemini-2.5-flash")
                    .sessionId(sessionId)
                    .build();
        } catch (Exception e) {
            log.error("Error in LLM chat: {}", e.getMessage(), e);
            return ChatAskResponse.builder()
                    .answer("Xin lỗi, đã xảy ra lỗi khi xử lý yêu cầu của bạn. Vui lòng thử lại.")
                    .usedTools(false)
                    .sessionId(sessionId)
                    .build();
        }
    }

    private ChatAskResponse tryHandleRuntimeToolCall(String message, String sessionId) {
        int sp = message.indexOf(' ');
        String head = sp > 0 ? message.substring(0, sp) : message;
        if (!head.startsWith("tool.")) return null;
        String json = sp > 0 ? message.substring(sp + 1).trim() : "";
        String tool = head.substring("tool.".length());
        try {
            return switch (tool) {
                case "searchFlights" -> {
                    var criteria = objectMapper.readValue(json, FlightSearchCriteria.class);
                    var result = searchFlightsTool.apply(criteria);
                    String summary = "Tìm thấy " + result.getTotalElements() + " chuyến.";
                    yield ChatAskResponse.builder().answer(summary).usedTools(true).sessionId(sessionId).build();
                }
                case "ragPolicy" -> {
                    var req = objectMapper.readValue(json, RagPolicyTool.Request.class);
                    String answer = ragPolicyTool.apply(req);
                    yield ChatAskResponse.builder().answer(answer).usedTools(true).sessionId(sessionId).build();
                }
                case "estimateCO2" -> {
                    var req = objectMapper.readValue(json, EstimateCO2Tool.Request.class);
                    var out = estimateCO2Tool.apply(req);
                    yield ChatAskResponse.builder().answer(objectMapper.writeValueAsString(out)).usedTools(true).sessionId(sessionId).build();
                }
                case "liveStatus" -> {
                    var req = objectMapper.readValue(json, LiveStatusTool.Request.class);
                    var out = liveStatusTool.apply(req);
                    yield ChatAskResponse.builder().answer(objectMapper.writeValueAsString(out)).usedTools(true).sessionId(sessionId).build();
                }
                case "generalKnowledge" -> {
                    var req = objectMapper.readValue(json, GeneralKnowledgeTool.Request.class);
                    String answer = generalKnowledgeTool.apply(req);
                    yield ChatAskResponse.builder().answer(answer).usedTools(true).sessionId(sessionId).build();
                }
                default -> ChatAskResponse.builder()
                        .answer("Tool không hỗ trợ: " + tool)
                        .usedTools(false)
                        .sessionId(sessionId)
                        .build();
            };
        } catch (Exception e) {
            log.warn("Runtime tool call failed: {}", e.getMessage());
            return ChatAskResponse.builder()
                    .answer("Gọi tool thất bại: " + e.getMessage())
                    .usedTools(false)
                    .sessionId(sessionId)
                    .build();
        }
    }

    private boolean looksLikePolicyQuestion(String text) {
        String t = text.toLowerCase();
        return t.contains("chính sách") || t.contains("policy")
                || t.contains("hành lý") || t.contains("baggage")
                || t.contains("đổi vé") || t.contains("hoàn vé");
    }

    private boolean looksLikeFlightSearch(String text) {
        String t = text.toUpperCase();
        // Check for explicit flight search keywords
        if (t.contains("TÌM VÉ") || t.contains("TÌM CHUYẾN") || t.contains("CHUYẾN BAY") 
            || t.contains("FLIGHT") || t.contains("TÌM CHUYẾN BAY")) {
            return true;
        }
        // Check for route pattern (e.g., "SGN-HAN", "HAN-SGN")
        if (t.matches(".*\\b[A-Z]{3}-[A-Z]{3}\\b.*")) {
            return true;
        }
        // Check for airport names or IATA codes
        String[] airports = {"SGN", "HAN", "DAD", "HPH", "VCA", "CXR", "PQC", "VCL", "DLI", "VDO", 
                            "TÂN SƠN NHẤT", "NỘI BÀI", "ĐÀ NẴNG", "PHÚ QUỐC", "CẦN THƠ", "VINH",
                            "SÀI GÒN", "TP.HCM", "TP HCM", "THÀNH PHỐ HỒ CHÍ MINH", "HÀ NỘI"};
        int airportCount = 0;
        for (String airport : airports) {
            if (t.contains(airport)) {
                airportCount++;
            }
        }
        // If contains airport names and flight-related keywords, likely flight search
        if (airportCount > 0 && (t.contains("ĐẾN") || t.contains("TỪ") || t.contains("ĐI") 
            || t.contains("TO") || t.contains("FROM") || t.contains("DANH SÁCH") 
            || t.contains("LIST") || t.contains("CÁC"))) {
            return true;
        }
        // Check for "các chuyến bay", "danh sách chuyến bay", etc.
        if (t.contains("CÁC CHUYẾN BAY") || t.contains("DANH SÁCH CHUYẾN BAY") 
            || t.contains("LIST FLIGHT") || t.contains("FLIGHT LIST")) {
            return true;
        }
        return false;
    }

    private FlightSearchCriteria parseBasicCriteria(String text) {
        var c = new FlightSearchCriteria();
        String t = text.toUpperCase();
        
        // Map airport names to IATA codes (with and without diacritics)
        Map<String, String> airportMap = new HashMap<>();
        // Tan Son Nhat / SGN
        airportMap.put("TÂN SƠN NHẤT", "SGN");
        airportMap.put("TAN SON NHAT", "SGN");
        airportMap.put("SÀI GÒN", "SGN");
        airportMap.put("SAI GON", "SGN");
        airportMap.put("TP.HCM", "SGN");
        airportMap.put("TP HCM", "SGN");
        airportMap.put("THÀNH PHỐ HỒ CHÍ MINH", "SGN");
        airportMap.put("THANH PHO HO CHI MINH", "SGN");
        // Noi Bai / HAN
        airportMap.put("NỘI BÀI", "HAN");
        airportMap.put("NOI BAI", "HAN");
        airportMap.put("HÀ NỘI", "HAN");
        airportMap.put("HA NOI", "HAN");
        // Other airports
        airportMap.put("ĐÀ NẴNG", "DAD");
        airportMap.put("DA NANG", "DAD");
        airportMap.put("PHÚ QUỐC", "PQC");
        airportMap.put("PHU QUOC", "PQC");
        airportMap.put("CẦN THƠ", "VCA");
        airportMap.put("CAN THO", "VCA");
        airportMap.put("VINH", "VII");
        airportMap.put("CÁT BI", "HPH");
        airportMap.put("CAT BI", "HPH");
        airportMap.put("CAM RANH", "CXR");
        airportMap.put("VÂN ĐỒN", "VDO");
        airportMap.put("VAN DON", "VDO");
        airportMap.put("ĐÀ LẠT", "DLI");
        airportMap.put("DA LAT", "DLI");
        airportMap.put("CHU LAI", "VCL");
        
        // Try to find route pattern first (e.g., "SGN-HAN")
        var routeMatcher = java.util.regex.Pattern.compile("\\b([A-Z]{3})-([A-Z]{3})\\b").matcher(t);
        if (routeMatcher.find()) {
            c.setDepIata(routeMatcher.group(1));
            c.setArrIata(routeMatcher.group(2));
        } else {
            // Try to parse from natural language
            String depIata = null;
            String arrIata = null;
            
            // Check for "đến" (to) or "từ" (from)
            if (t.contains("ĐẾN")) {
                // Find airport after "đến"
                int toIndex = t.indexOf("ĐẾN");
                String afterTo = t.substring(toIndex + 3).trim();
                // Remove common words like "sân bay", "airport", etc.
                afterTo = afterTo.replace("SÂN BAY", "").replace("AIRPORT", "").trim();
                for (var entry : airportMap.entrySet()) {
                    if (afterTo.contains(entry.getKey()) || afterTo.startsWith(entry.getValue())) {
                        arrIata = entry.getValue();
                        break;
                    }
                }
                // Find airport before "đến" (departure)
                String beforeTo = t.substring(0, toIndex);
                beforeTo = beforeTo.replace("SÂN BAY", "").replace("AIRPORT", "").trim();
                for (var entry : airportMap.entrySet()) {
                    if (beforeTo.contains(entry.getKey()) || beforeTo.contains(entry.getValue())) {
                        depIata = entry.getValue();
                        break;
                    }
                }
            } else if (t.contains("TỪ")) {
                // Find airport after "từ"
                int fromIndex = t.indexOf("TỪ");
                String afterFrom = t.substring(fromIndex + 3).trim();
                // Remove common words like "sân bay", "airport", etc.
                afterFrom = afterFrom.replace("SÂN BAY", "").replace("AIRPORT", "").trim();
                // Check if there's "đến" after "từ"
                if (afterFrom.contains("ĐẾN")) {
                    int toIndex = afterFrom.indexOf("ĐẾN");
                    String depStr = afterFrom.substring(0, toIndex).trim();
                    String arrStr = afterFrom.substring(toIndex + 3).trim();
                    arrStr = arrStr.replace("SÂN BAY", "").replace("AIRPORT", "").trim();
                    for (var entry : airportMap.entrySet()) {
                        if (depStr.contains(entry.getKey()) || depStr.startsWith(entry.getValue())) {
                            depIata = entry.getValue();
                            break;
                        }
                    }
                    for (var entry : airportMap.entrySet()) {
                        if (arrStr.contains(entry.getKey()) || arrStr.startsWith(entry.getValue())) {
                            arrIata = entry.getValue();
                            break;
                        }
                    }
                } else {
                    for (var entry : airportMap.entrySet()) {
                        if (afterFrom.contains(entry.getKey()) || afterFrom.startsWith(entry.getValue())) {
                            depIata = entry.getValue();
                            break;
                        }
                    }
                }
            }
            
            // If only arrival is specified (e.g., "các chuyến bay đến Tân Sơn Nhất")
            if (arrIata != null && depIata == null) {
                c.setArrIata(arrIata);
                // Don't set departure - search all flights to this airport
            } else if (depIata != null && arrIata != null) {
                c.setDepIata(depIata);
                c.setArrIata(arrIata);
            } else if (depIata != null) {
                c.setDepIata(depIata);
            }
        }
        
        // Parse date
        var dateMatcher = Pattern.compile("(20\\d{2}-\\d{2}-\\d{2})").matcher(text);
        if (dateMatcher.find()) {
            c.setSnapshotDate(LocalDate.parse(dateMatcher.group(1)));
        } else {
            // Try to parse Vietnamese date format: "ngày 12 tháng 11 năm 2025"
            var vnDatePattern = Pattern.compile("NGÀY\\s+(\\d{1,2})\\s+THÁNG\\s+(\\d{1,2})\\s+NĂM\\s+(\\d{4})", Pattern.CASE_INSENSITIVE);
            var vnDateMatcher = vnDatePattern.matcher(t);
            if (vnDateMatcher.find()) {
                int day = Integer.parseInt(vnDateMatcher.group(1));
                int month = Integer.parseInt(vnDateMatcher.group(2));
                int year = Integer.parseInt(vnDateMatcher.group(3));
                try {
                    c.setSnapshotDate(LocalDate.of(year, month, day));
                } catch (Exception e) {
                    log.warn("Invalid date: {}-{}-{}", year, month, day);
                    c.setSnapshotDate(LocalDate.now().plusDays(1));
                }
            } else {
                // Check for "hôm nay", "hôm qua", "ngày mai", etc.
                if (t.contains("HÔM NAY") || t.contains("HOM NAY") || t.contains("TODAY")) {
                    c.setSnapshotDate(LocalDate.now());
                } else if (t.contains("HÔM QUA") || t.contains("HOM QUA") || t.contains("YESTERDAY")) {
                    c.setSnapshotDate(LocalDate.now().minusDays(1));
                } else if (t.contains("NGÀY MAI") || t.contains("NGAY MAI") || t.contains("TOMORROW")) {
                    c.setSnapshotDate(LocalDate.now().plusDays(1));
                } else {
                    c.setSnapshotDate(LocalDate.now().plusDays(1));
                }
            }
        }
        
        return c;
    }

    private String formatFlightSample(FlightSearchResult result) {
        var f = result.getFlights().get(0);
        String price = f.getPriceCents() != null ? (f.getPriceCents() / 100) + "₫" : "N/A";
        return f.getCarrier() + " " + f.getFlightNo() + " • " + f.getDepIata() + "→" + f.getArrIata()
                + " • " + (f.getDepTime() != null ? f.getDepTime() : "") + " • " + price;
    }

    /**
     * Check if the message looks like a general question (not specifically about flights or policies)
     */
    private boolean looksLikeGeneralQuestion(String text) {
        String t = text.toLowerCase();
        
        // If it's clearly a flight or policy question, return false
        if (looksLikeFlightSearch(text) || looksLikePolicyQuestion(text)) {
            return false;
        }
        
        // General question indicators
        return t.contains("là gì") || t.contains("là ai") || t.contains("như thế nào")
                || t.contains("tại sao") || t.contains("vì sao") || t.contains("khi nào")
                || t.contains("ở đâu") || t.contains("bao nhiêu") || t.contains("có thể")
                || t.contains("cách") || t.contains("hướng dẫn") || t.contains("giúp")
                || t.contains("thông tin") || t.contains("dịch vụ") || t.contains("check-in")
                || t.contains("checkin") || t.contains("sân bay") || t.contains("airport")
                || t.contains("thủ tục") || t.contains("cần") || t.contains("phải")
                || t.matches(".*\\?.*") || t.matches(".*\\?$"); // Ends with question mark
    }

    /**
     * Compose a natural answer using LLM with retrieved context
     */
    private String composeAnswerWithContext(String question, String context, 
                                           List<ConversationMessage> history) {
        try {
            StringBuilder systemPrompt = new StringBuilder();
            systemPrompt.append("Bạn là trợ lý tư vấn khách hàng chuyên nghiệp của hãng hàng không.\n");
            systemPrompt.append("Nhiệm vụ của bạn là trả lời câu hỏi dựa trên thông tin được cung cấp.\n\n");
            systemPrompt.append("Hướng dẫn:\n");
            systemPrompt.append("1. Trả lời dựa trên CONTEXT được cung cấp\n");
            systemPrompt.append("2. Nếu CONTEXT không đủ, hãy trả lời dựa trên kiến thức chung về hàng không\n");
            systemPrompt.append("3. Trả lời một cách tự nhiên, thân thiện, dễ hiểu\n");
            systemPrompt.append("4. Sử dụng ngôn ngữ tiếng Việt tự nhiên\n");
            systemPrompt.append("5. Nếu không chắc chắn, hãy đề xuất khách hàng liên hệ trực tiếp\n");
            
            StringBuilder userPrompt = new StringBuilder();
            if (!history.isEmpty()) {
                userPrompt.append("Ngữ cảnh cuộc hội thoại trước:\n");
                List<ChatAskRequest.ChatMessage> chatHistory = conversationService.toChatMessages(history);
                for (ChatAskRequest.ChatMessage histMsg : chatHistory) {
                    if ("user".equals(histMsg.getRole())) {
                        userPrompt.append("Khách hàng: ").append(histMsg.getContent()).append("\n");
                    } else if ("assistant".equals(histMsg.getRole())) {
                        userPrompt.append("Trợ lý: ").append(histMsg.getContent()).append("\n");
                    }
                }
                userPrompt.append("\n");
            }
            
            userPrompt.append("CÂU HỎI: ").append(question).append("\n\n");
            userPrompt.append("THÔNG TIN THAM KHẢO:\n").append(context).append("\n\n");
            userPrompt.append("Hãy trả lời câu hỏi một cách tự nhiên và hữu ích.");
            
            return chatClient.prompt()
                    .system(systemPrompt.toString())
                    .user(userPrompt.toString())
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Error composing answer with context: {}", e.getMessage(), e);
            // Fallback: return context directly
            return "Dựa trên thông tin tìm được:\n\n" + context;
        }
    }


    private String buildConversationSummary(List<ConversationMessage> history) {
        StringBuilder stringBuilder = new StringBuilder();
        for(ConversationMessage message: history) {
            stringBuilder.append(message.getRole() == ConversationMessage.MessageRole.USER ? "Khách: ": "Trợ lý: ");
            stringBuilder.append(message.getContent()).append("\n\n");
            if(stringBuilder.length() > CONTEXT_CHAR_BUDGET) break;
        }

        return stringBuilder.toString().trim();
    }
}
