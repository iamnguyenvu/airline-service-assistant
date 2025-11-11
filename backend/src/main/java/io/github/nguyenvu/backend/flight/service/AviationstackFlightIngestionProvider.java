package io.github.nguyenvu.backend.flight.service;

import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AviationstackFlightIngestionProvider implements FlightIngestionProvider {

    @Value("${app.ingestion.aviationstack.api-key:}")
    private String apiKey;

    @Value("${app.ingestion.aviationstack.base-url:https://api.aviationstack.com/v1}")
    private String baseUrl;

    @Value("${app.ingestion.routes:}")
    private String routesCsv;

    private final RestTemplate restTemplate = new RestTemplate();

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
        List<String> routes = parseRoutes();
        if (routes.isEmpty()) {
            log.warn("No routes configured in app.ingestion.routes; returning empty result");
            return List.of();
        }
        log.info("Aviationstack fetchDaily date={} routes={}", date, routes);
        List<FlightSnapshot> out = new ArrayList<>();
        for (String route : routes) {
            String[] parts = route.split("-");
            if (parts.length != 2) continue;
            String dep = parts[0].trim().toUpperCase();
            String arr = parts[1].trim().toUpperCase();
            try {
                out.addAll(fetchRoute(date, dep, arr));
            } catch (Exception e) {
                log.warn("Fetch failed for route {}: {}", route, e.getMessage());
            }
        }
        return out;
    }

    private List<FlightSnapshot> fetchRoute(LocalDate date, String depIata, String arrIata) {
        String url = String.format("%s/flights?access_key=%s&dep_iata=%s&arr_iata=%s&flight_status=scheduled&flight_date=%s",
                baseUrl, apiKey, depIata, arrIata, date);
        ResponseEntity<JsonNode> resp = restTemplate.getForEntity(url, JsonNode.class);
        JsonNode root = resp.getBody();
        if (root == null || root.get("data") == null || !root.get("data").isArray()) {
            return List.of();
        }
        List<FlightSnapshot> list = new ArrayList<>();
        for (JsonNode node : root.get("data")) {
            FlightSnapshot snapshot = mapToSnapshot(node, date, depIata, arrIata);
            if (snapshot != null) list.add(snapshot);
        }
        return list;
    }

    private FlightSnapshot mapToSnapshot(JsonNode n, LocalDate date, String depIata, String arrIata) {
        try {
            String carrier = text(n, "airline", "iata");
            String flightNo = text(n, "flight", "iata");
            String depTimeStr = text(n, "departure", "scheduled");
            String arrTimeStr = text(n, "arrival", "scheduled");
            LocalDateTime depTime = parseIso(depTimeStr);
            LocalDateTime arrTime = parseIso(arrTimeStr);
            if (depTime == null || arrTime == null) return null;
            int durationMin = (int) java.time.Duration.between(depTime, arrTime).toMinutes();
            if (durationMin <= 0) return null;

            return FlightSnapshot.builder()
                    .snapshotDate(date)
                    .depIata(depIata)
                    .arrIata(arrIata)
                    .depTime(depTime)
                    .arrTime(arrTime)
                    .carrier(safe3(carrier))
                    .flightNo(flightNo != null ? flightNo : "")
                    .durationMin(durationMin)
                    .stops((short) 0)
                    .fareFamily(null)
                    .baggageKg(null)
                    .priceCents(0L)
                    .currency("VND")
                    .source("aviationstack")
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private static String text(JsonNode node, String... path) {
        JsonNode cur = node;
        for (String p : path) {
            if (cur == null) return null;
            cur = cur.get(p);
        }
        return cur != null && !cur.isNull() ? cur.asText() : null;
    }

    private static LocalDateTime parseIso(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(s.replace("Z", ""), DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception ignore) {
                return null;
            }
        }
    }

    private static String safe3(String s) {
        if (s == null) return "";
        String t = s.trim().toUpperCase();
        return t.length() > 3 ? t.substring(0, 3) : t;
    }

    private List<String> parseRoutes() {
        if (routesCsv == null || routesCsv.isBlank()) return List.of();
        return Arrays.stream(routesCsv.split(","))
                .map(String::trim)
                .filter(r -> r.matches("^[A-Za-z]{3}-[A-Za-z]{3}$"))
                .toList();
    }
}

