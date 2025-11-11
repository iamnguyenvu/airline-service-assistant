package io.github.nguyenvu.backend.flight.controller;

import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import io.github.nguyenvu.backend.flight.service.FlightIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ingestion")
@RequiredArgsConstructor
public class IngestionController {

    private final FlightIngestionService ingestionService;

    @GetMapping("/test")
    @Operation(summary = "Test ingestion provider fetch without persisting")
    public ResponseEntity<Map<String, Object>> testProvider(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "amadeus") String provider,
            @RequestParam(required = false) String dep,
            @RequestParam(required = false) String arr
    ) {
        String providerName = provider.trim().toLowerCase();
        List<FlightSnapshot> snapshots = ingestionService.testFetch(
                providerName,
                date,
                dep != null ? dep.trim().toUpperCase() : null,
                arr != null ? arr.trim().toUpperCase() : null
        );
        List<Map<String, Object>> sample = snapshots.stream().limit(3).map(s -> {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("route", s.getDepIata() + "-" + s.getArrIata());
            m.put("flightNo", s.getFlightNo());
            m.put("carrier", s.getCarrier());
            m.put("depTime", s.getDepTime());
            m.put("arrTime", s.getArrTime());
            m.put("durationMin", s.getDurationMin());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of(
                "date", date.toString(),
                "provider", providerName,
                "count", snapshots.size(),
                "sample", sample
        ));
    }
}


