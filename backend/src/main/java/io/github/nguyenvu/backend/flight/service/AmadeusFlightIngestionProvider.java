package io.github.nguyenvu.backend.flight.service;

import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AmadeusFlightIngestionProvider implements FlightIngestionProvider {

    private final AmadeusAuthService authService;
    private final RestTemplate restTemplate;

    @Value("${app.ingestion.routes:}")
    private String routesCsv;

    @Value("${app.ingestion.amadeus.base-url:https://test.api.amadeus.com}")
    private String baseUrl;

    @Override
    public String name() {
        return "amadeus";
    }

    @Override
    public List<FlightSnapshot> fetchDaily(LocalDate date) {
        if (!authService.isConfigured()) {
            log.warn("Amadeus credentials missing; returning empty result");
            return List.of();
        }
        List<String> routes = parseRoutes();
        if (routes.isEmpty()) {
            log.warn("No routes configured in app.ingestion.routes; returning empty result");
            return List.of();
        }
        List<FlightSnapshot> out = new ArrayList<>();
        for (String route : routes) {
            String[] parts = route.split("-");
            if (parts.length != 2) continue;
            String dep = parts[0].trim().toUpperCase();
            String arr = parts[1].trim().toUpperCase();
            try {
                out.addAll(fetchRoute(date, dep, arr));
            } catch (Exception e) {
                log.warn("Amadeus fetch failed for {}-{}: {}", dep, arr, e.getMessage());
            }
        }
        return out;
    }

    @Override
    public List<FlightSnapshot> fetchRoute(LocalDate date, String depIata, String arrIata) {
        if (!authService.isConfigured()) {
            throw new IllegalStateException("Amadeus credentials are not configured");
        }
        String token = authService.getAccessToken();
        String url = baseUrl + "/v1/shopping/availability/flight-availabilities";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        Map<String, Object> payload = buildAvailabilityPayload(date, depIata, arrIata);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> body = response.getBody();
        if (body == null || !(body.get("data") instanceof List<?> dataList)) {
            log.info("Amadeus availability returned empty data for {}-{} {}", depIata, arrIata, date);
            return List.of();
        }
        List<FlightSnapshot> snapshots = new ArrayList<>();
        for (Object item : dataList) {
            if (!(item instanceof Map<?, ?> flightMap)) continue;
            Object segmentsObj = flightMap.get("availabilitySegments");
            if (!(segmentsObj instanceof List<?> segments)) continue;
            for (Object segObj : segments) {
                if (!(segObj instanceof Map<?, ?> segment)) continue;
                FlightSnapshot snapshot = mapSegment(segment, date, depIata, arrIata);
                if (snapshot != null) {
                    snapshots.add(snapshot);
                }
            }
        }
        log.info("Amadeus fetched {} snapshots for {}-{} {}", snapshots.size(), depIata, arrIata, date);
        return snapshots;
    }

    private Map<String, Object> buildAvailabilityPayload(LocalDate date, String dep, String arr) {
        Map<String, Object> originDestination = new LinkedHashMap<>();
        originDestination.put("id", "1");
        originDestination.put("originLocationCode", dep);
        originDestination.put("destinationLocationCode", arr);
        Map<String, Object> departureDateTime = new LinkedHashMap<>();
        departureDateTime.put("date", date.toString());
        departureDateTime.put("time", "00:00:00");
        originDestination.put("departureDateTime", departureDateTime);

        Map<String, Object> traveler = new LinkedHashMap<>();
        traveler.put("id", "1");
        traveler.put("travelerType", "ADULT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("originDestinations", List.of(originDestination));
        payload.put("travelers", List.of(traveler));
        payload.put("sources", List.of("GDS"));
        return payload;
    }

    private FlightSnapshot mapSegment(Map<?, ?> segment, LocalDate date, String depIata, String arrIata) {
        try {
            String carrier = text(segment, "flightDesignator", "carrierCode");
            String flightNumber = text(segment, "flightDesignator", "flightNumber");
            if (flightNumber == null) {
                flightNumber = text(segment, "number");
            }
            Map<?, ?> departure = objectMap(segment.get("departure"));
            Map<?, ?> arrival = objectMap(segment.get("arrival"));
            LocalDateTime depTime = parseDateTime(departure);
            LocalDateTime arrTime = parseDateTime(arrival);
            if (depTime == null || arrTime == null) {
                return null;
            }
            int durationMin = (int) java.time.Duration.between(depTime, arrTime).toMinutes();
            if (durationMin <= 0) {
                return null;
            }
            Map<?, ?> firstClass = firstAvailabilityClass(segment);
            String cabin = firstClass != null ? string(firstClass.get("cabin")) : null;
            String bookingClass = firstClass != null ? string(firstClass.get("class")) : null;
            Integer nbSeats = firstClass != null ? intValue(firstClass.get("nbSeats")) : null;

            return FlightSnapshot.builder()
                    .snapshotDate(date)
                    .depIata(depIata)
                    .arrIata(arrIata)
                    .depTime(depTime)
                    .arrTime(arrTime)
                    .carrier(carrier != null ? carrier : "")
                    .flightNo(carrier != null && flightNumber != null ? carrier + flightNumber : flightNumber != null ? flightNumber : "")
                    .durationMin(durationMin)
                    .stops((short) 0)
                    .fareFamily(cabin != null ? cabin : bookingClass)
                    .baggageKg(nbSeats != null ? BigDecimal.ZERO : null)
                    .priceCents(0L)
                    .currency("USD")
                    .source("amadeus")
                    .build();
        } catch (Exception e) {
            log.debug("Failed to map Amadeus segment: {}", e.getMessage());
            return null;
        }
    }

    private Map<?, ?> firstAvailabilityClass(Map<?, ?> segment) {
        Object classes = segment.get("availabilityClasses");
        if (classes instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> map) {
            return (Map<?, ?>) map;
        }
        return null;
    }

    private Map<?, ?> objectMap(Object node) {
        return node instanceof Map<?, ?> map ? map : Collections.emptyMap();
    }

    private LocalDateTime parseDateTime(Map<?, ?> node) {
        if (node == null) return null;
        String value = string(node.get("dateTime"));
        if (value == null) value = string(node.get("at"));
        if (value == null) return null;
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }

    private String text(Map<?, ?> node, String... path) {
        Map<?, ?> current = node;
        for (int i = 0; i < path.length - 1; i++) {
            Object next = current.get(path[i]);
            if (!(next instanceof Map<?, ?> nextMap)) {
                return null;
            }
            current = nextMap;
        }
        Object leaf = current.get(path[path.length - 1]);
        return string(leaf);
    }

    private String string(Object value) {
        return value != null ? value.toString() : null;
    }

    private Integer intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    private List<String> parseRoutes() {
        if (routesCsv == null || routesCsv.isBlank()) return List.of();
        return Arrays.stream(routesCsv.split(","))
                .map(String::trim)
                .filter(r -> r.matches("^[A-Za-z]{3}-[A-Za-z]{3}$"))
                .toList();
    }
}


