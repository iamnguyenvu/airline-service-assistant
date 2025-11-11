package io.github.nguyenvu.backend.ai.service;

import io.github.nguyenvu.backend.ai.dto.ChatAskRequest;
import io.github.nguyenvu.backend.ai.dto.ChatAskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.nguyenvu.backend.ai.tool.EstimateCO2Tool;
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
    private final EstimateCO2Tool estimateCO2Tool;
    private final LiveStatusTool liveStatusTool;
    private final ObjectMapper objectMapper;

    public ChatAskResponse ask(ChatAskRequest req) {
        // Prefer calling domain tools/services directly when intent is clear.
        // If not clear, fall back to LLM for guidance.
        String message = req.getMessage() != null ? req.getMessage().trim() : "";
        if (message.isBlank()) {
            return ChatAskResponse.builder()
                    .answer("Bạn hãy nhập câu hỏi hoặc yêu cầu cụ thể nhé.")
                    .usedTools(false)
                    .sessionId(req.getSessionId())
                    .build();
        }

        // Runtime tool-first: explicit tool call "tool.<name> {json}"
        ChatAskResponse runtimeTool = tryHandleRuntimeToolCall(message, req.getSessionId());
        if (runtimeTool != null) {
            return runtimeTool;
        }

        if (looksLikePolicyQuestion(message)) {
            // Tool-first: route to policy retriever+composer
            String answer = policyQAService.ask(message);
            return ChatAskResponse.builder()
                    .answer(answer)
                    .usedTools(true)
                    .sessionId(req.getSessionId())
                    .build();
        }

        if (looksLikeFlightSearch(message)) {
            // Tool-first: route to flight search service
            var criteria = parseBasicCriteria(message);
            var result = flightSearchService.search(criteria, 0, 10);
            String answer = "Tìm thấy " + result.getTotalElements() +
                    " chuyến phù hợp. Ví dụ: " +
                    (result.getFlights().isEmpty() ? "không có chuyến phù hợp." :
                            formatFlightSample(result));
            return ChatAskResponse.builder()
                    .answer(answer)
                    .usedTools(true)
                    .sessionId(req.getSessionId())
                    .flightResults(result)
                    .build();
        }

        // Fallback: Use LLM with automatic tool calling
        try {
            var result = chatClient
                    .prompt()
                    .user(message)
                    .call()
                    .content();

            boolean usedTools = result != null && result.contains("[Tool]") || 
                               message.toLowerCase().contains("tool.");

            return ChatAskResponse.builder()
                    .answer(result != null ? result : "Xin lỗi, tôi không thể xử lý yêu cầu này.")
                    .usedTools(usedTools)
                    .model("gemini-2.5-flash")
                    .sessionId(req.getSessionId())
                    .build();
        } catch (Exception e) {
            log.error("Error in LLM chat: {}", e.getMessage(), e);
            return ChatAskResponse.builder()
                    .answer("Xin lỗi, đã xảy ra lỗi khi xử lý yêu cầu của bạn. Vui lòng thử lại.")
                    .usedTools(false)
                    .sessionId(req.getSessionId())
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
                    var criteria = objectMapper.readValue(json, io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria.class);
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

    private io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria parseBasicCriteria(String text) {
        var c = new io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria();
        String t = text.toUpperCase();
        
        // Map airport names to IATA codes
        java.util.Map<String, String> airportMap = new java.util.HashMap<>();
        airportMap.put("TÂN SƠN NHẤT", "SGN");
        airportMap.put("SÀI GÒN", "SGN");
        airportMap.put("TP.HCM", "SGN");
        airportMap.put("TP HCM", "SGN");
        airportMap.put("THÀNH PHỐ HỒ CHÍ MINH", "SGN");
        airportMap.put("NỘI BÀI", "HAN");
        airportMap.put("HÀ NỘI", "HAN");
        airportMap.put("HÀ NỘI", "HAN");
        airportMap.put("ĐÀ NẴNG", "DAD");
        airportMap.put("PHÚ QUỐC", "PQC");
        airportMap.put("CẦN THƠ", "VCA");
        airportMap.put("VINH", "VII");
        airportMap.put("CÁT BI", "HPH");
        airportMap.put("CAM RANH", "CXR");
        airportMap.put("VÂN ĐỒN", "VDO");
        airportMap.put("ĐÀ LẠT", "DLI");
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
        var dateMatcher = java.util.regex.Pattern.compile("(20\\d{2}-\\d{2}-\\d{2})").matcher(text);
        if (dateMatcher.find()) {
            c.setSnapshotDate(java.time.LocalDate.parse(dateMatcher.group(1)));
        } else {
            // Try to parse Vietnamese date format: "ngày 12 tháng 11 năm 2025"
            var vnDatePattern = java.util.regex.Pattern.compile("NGÀY\\s+(\\d{1,2})\\s+THÁNG\\s+(\\d{1,2})\\s+NĂM\\s+(\\d{4})", java.util.regex.Pattern.CASE_INSENSITIVE);
            var vnDateMatcher = vnDatePattern.matcher(t);
            if (vnDateMatcher.find()) {
                int day = Integer.parseInt(vnDateMatcher.group(1));
                int month = Integer.parseInt(vnDateMatcher.group(2));
                int year = Integer.parseInt(vnDateMatcher.group(3));
                try {
                    c.setSnapshotDate(java.time.LocalDate.of(year, month, day));
                } catch (Exception e) {
                    log.warn("Invalid date: {}-{}-{}", year, month, day);
                    c.setSnapshotDate(java.time.LocalDate.now().plusDays(1));
                }
            } else {
                // Check for "hôm nay", "ngày mai", etc.
                if (t.contains("HÔM NAY") || t.contains("TODAY")) {
                    c.setSnapshotDate(java.time.LocalDate.now());
                } else if (t.contains("NGÀY MAI") || t.contains("TOMORROW")) {
                    c.setSnapshotDate(java.time.LocalDate.now().plusDays(1));
                } else {
                    c.setSnapshotDate(java.time.LocalDate.now().plusDays(1));
                }
            }
        }
        
        return c;
    }

    private String formatFlightSample(io.github.nguyenvu.backend.flight.dto.FlightSearchResult result) {
        var f = result.getFlights().get(0);
        String price = f.getPriceCents() != null ? (f.getPriceCents() / 100) + "₫" : "N/A";
        return f.getCarrier() + " " + f.getFlightNo() + " • " + f.getDepIata() + "→" + f.getArrIata()
                + " • " + (f.getDepTime() != null ? f.getDepTime() : "") + " • " + price;
    }
}
