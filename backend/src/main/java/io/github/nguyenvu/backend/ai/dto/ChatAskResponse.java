package io.github.nguyenvu.backend.ai.dto;

import io.github.nguyenvu.backend.flight.dto.FlightSearchResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatAskResponse {
    private String answer;
    private boolean usedTools;
    private String model;
    private String sessionId;
    private FlightSearchResult flightResults;
}
