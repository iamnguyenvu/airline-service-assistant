package io.github.nguyenvu.backend.flight.service;

import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AviationstackFlightIngestionProvider implements FlightIngestionProvider {

    @Value("${app.ingestion.aviationstack.api-key:}")
    private String apiKey;

    @Value("${app.ingestion.aviationstack.base-url:https://api.aviationstack.com/v1}")
    private String baseUrl;

    @Override
    public String name() {
        return "aviationstack";
    }

    @Override
    public List<FlightSnapshot> fetchDaily(LocalDate date) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Aviationstack API key not configured; returning empty result");
            return List.of();
        }
        // Integrate REST client in a follow-up and map to FlightSnapshot list.
        log.info("Aviationstack fetchDaily invoked for date={} baseUrl={}", date, baseUrl);
        return List.of();
    }
}

