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
                    .build();
        }

        // Fallback: LLM-only response (no registered tools to avoid bean cycles)
        var result = chatClient
                .prompt()
                .user(message)
                .call();

        String answer = result.content();
        String model = null;
        boolean usedTools = false;

        return ChatAskResponse.builder()
                .answer(answer)
                .usedTools(usedTools)
                .model(model)
                .sessionId(req.getSessionId())
                .build();
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
        return t.matches(".*\\b[A-Z]{3}-[A-Z]{3}\\b.*") || t.contains("TÌM VÉ") || t.contains("TÌM CHUYẾN");
    }

    private io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria parseBasicCriteria(String text) {
        var c = new io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria();
        String t = text.toUpperCase();
        var routeMatcher = java.util.regex.Pattern.compile("\\b([A-Z]{3})-([A-Z]{3})\\b").matcher(t);
        if (routeMatcher.find()) {
            c.setDepIata(routeMatcher.group(1));
            c.setArrIata(routeMatcher.group(2));
        }
        var dateMatcher = java.util.regex.Pattern.compile("(20\\d{2}-\\d{2}-\\d{2})").matcher(text);
        if (dateMatcher.find()) {
            c.setSnapshotDate(java.time.LocalDate.parse(dateMatcher.group(1)));
        } else {
            c.setSnapshotDate(java.time.LocalDate.now().plusDays(1));
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
