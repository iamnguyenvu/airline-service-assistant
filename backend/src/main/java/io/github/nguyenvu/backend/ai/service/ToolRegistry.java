package io.github.nguyenvu.backend.ai.service;

import io.github.nguyenvu.backend.ai.tool.EstimateCO2Tool;
import io.github.nguyenvu.backend.ai.tool.LiveStatusTool;
import io.github.nguyenvu.backend.ai.tool.RagPolicyTool;
import io.github.nguyenvu.backend.ai.tool.SearchFlightsTool;
import io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria;
import io.github.nguyenvu.backend.flight.dto.FlightSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ToolRegistry {

    private final SearchFlightsTool searchFlightsTool;
    private final RagPolicyTool ragPolicyTool;
    private final EstimateCO2Tool estimateCO2Tool;
    private final LiveStatusTool liveStatusTool;

    public FlightSearchResult searchFlights(FlightSearchCriteria criteria) {
        return searchFlightsTool.apply(criteria);
    }

    public String ragPolicy(String query, String airline, String docType, Integer topK) {
        RagPolicyTool.Request req = new RagPolicyTool.Request();
        req.setQuery(query);
        req.setAirline(airline);
        req.setDocType(docType);
        req.setTopK(topK);
        return ragPolicyTool.apply(req);
    }

    public Map<String, Object> estimateCO2(String from, String to, Integer durationMin) {
        EstimateCO2Tool.Request req = new EstimateCO2Tool.Request();
        req.setFromIata(from);
        req.setToIata(to);
        req.setDurationMin(durationMin);
        return estimateCO2Tool.apply(req);
    }

    public Map<String, Object> liveStatus(String flightNo, String date) {
        LiveStatusTool.Request req = new LiveStatusTool.Request();
        req.setFlightNo(flightNo);
        req.setDate(date);
        return liveStatusTool.apply(req);
    }
}


