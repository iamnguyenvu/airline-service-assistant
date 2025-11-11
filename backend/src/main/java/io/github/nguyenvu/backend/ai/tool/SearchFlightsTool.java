package io.github.nguyenvu.backend.ai.tool;

import io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria;
import io.github.nguyenvu.backend.flight.dto.FlightSearchResult;
import io.github.nguyenvu.backend.flight.service.FlightSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component("searchFlights")
@RequiredArgsConstructor
public class SearchFlightsTool implements Function<FlightSearchCriteria, FlightSearchResult> {

    private final FlightSearchService flightSearchService;

    @Override
    public FlightSearchResult apply(FlightSearchCriteria criteria) {
        log.info("[Tool] searchFlights {}", criteria);
        return flightSearchService.search(criteria, 0, 20);
    }
}


