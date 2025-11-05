package io.github.nguyenvu.backend.flight.controller;

import io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria;
import io.github.nguyenvu.backend.flight.dto.FlightSearchResult;
import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import io.github.nguyenvu.backend.flight.repository.PriceStatistics;
import io.github.nguyenvu.backend.flight.service.FlightSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST API for flight search operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Tag(name = "Flight Search", description = "Search and query flight information")
public class FlightSearchController {

    private final FlightSearchService flightSearchService;

    /**
     * Search flights with flexible criteria.
     * 
     * POST /api/flights/search
     */
    @PostMapping("/search")
    @Operation(
        summary = "Search flights",
        description = "Search flights with multiple criteria and intelligent ranking",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Flights found",
                content = @Content(schema = @Schema(implementation = FlightSearchResult.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid search criteria")
        }
    )
    public ResponseEntity<FlightSearchResult> searchFlights(
            @Valid @RequestBody FlightSearchCriteria criteria,
            @RequestParam(defaultValue = "0") 
                @Parameter(description = "Page number (0-indexed)") int page,
            @RequestParam(defaultValue = "20") 
                @Parameter(description = "Page size (max 100)") int size) {
        
        log.info("POST /api/flights/search - criteria: {}, page: {}, size: {}", 
            criteria, page, size);
        
        size = Math.min(size, 100);
        
        FlightSearchResult result = flightSearchService.search(criteria, page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * Get cheapest flights for a route and date.
     * 
     * GET /api/flights/cheapest?dep=SGN&arr=HAN&date=2025-12-01&limit=5
     */
    @GetMapping("/cheapest")
    @Operation(
        summary = "Get cheapest flights",
        description = "Find the cheapest flights for a specific route and date"
    )
    public ResponseEntity<List<FlightSnapshot>> getCheapestFlights(
            @RequestParam 
                @Parameter(description = "Departure airport IATA", example = "SGN", required = true) 
                String dep,
            @RequestParam 
                @Parameter(description = "Arrival airport IATA", example = "HAN", required = true) 
                String arr,
            @RequestParam 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                @Parameter(description = "Flight date", example = "2025-12-01", required = true) 
                LocalDate date,
            @RequestParam(defaultValue = "5") 
                @Parameter(description = "Number of results (max 20)", example = "5") 
                int limit) {
        
        log.info("GET /api/flights/cheapest - {}-{} on {}, limit: {}", 
            dep, arr, date, limit);
        
        dep = dep.toUpperCase().trim();
        arr = arr.toUpperCase().trim();
        limit = Math.min(limit, 20);
        
        if (dep.length() != 3 || arr.length() != 3) {
            throw new IllegalArgumentException("Invalid IATA code format");
        }
        
        List<FlightSnapshot> flights = flightSearchService.findCheapestFlights(
            dep, arr, date, limit);
        
        return ResponseEntity.ok(flights);
    }

    /**
     * Get available carriers for a route.
     * 
     * GET /api/flights/carriers?dep=SGN&arr=HAN
     */
    @GetMapping("/carriers")
    @Operation(
        summary = "Get available carriers",
        description = "List all carriers operating on a specific route"
    )
    public ResponseEntity<List<String>> getCarriers(
            @RequestParam 
                @Parameter(description = "Departure airport IATA", example = "SGN", required = true) 
                String dep,
            @RequestParam 
                @Parameter(description = "Arrival airport IATA", example = "HAN", required = true) 
                String arr) {
        
        log.info("GET /api/flights/carriers - {}-{}", dep, arr);
        
        dep = dep.toUpperCase().trim();
        arr = arr.toUpperCase().trim();
        
        List<String> carriers = flightSearchService.getAvailableCarriers(dep, arr);
        return ResponseEntity.ok(carriers);
    }

    /**
     * Check if flights are available for a route and date.
     * 
     * GET /api/flights/availability?dep=SGN&arr=HAN&date=2025-12-01
     */
    @GetMapping("/availability")
    @Operation(
        summary = "Check flight availability",
        description = "Check if any flights exist for a route and date"
    )
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @RequestParam 
                @Parameter(description = "Departure airport IATA", example = "SGN") 
                String dep,
            @RequestParam 
                @Parameter(description = "Arrival airport IATA", example = "HAN") 
                String arr,
            @RequestParam 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                @Parameter(description = "Flight date", example = "2025-12-01") 
                LocalDate date) {
        
        log.info("GET /api/flights/availability - {}-{} on {}", dep, arr, date);
        
        dep = dep.toUpperCase().trim();
        arr = arr.toUpperCase().trim();
        
        boolean available = flightSearchService.hasFlights(dep, arr, date);
        
        return ResponseEntity.ok(Map.of(
            "available", available,
            "route", dep + "-" + arr,
            "date", date.toString()
        ));
    }

    /**
     * Get price statistics for a route and date.
     * 
     * GET /api/flights/price-stats?dep=SGN&arr=HAN&date=2025-12-01
     */
    @GetMapping("/price-stats")
    @Operation(
        summary = "Get price statistics",
        description = "Get min, average, and max prices for a route and date"
    )
    public ResponseEntity<PriceStatistics> getPriceStatistics(
            @RequestParam 
                @Parameter(description = "Departure airport IATA", example = "SGN") 
                String dep,
            @RequestParam 
                @Parameter(description = "Arrival airport IATA", example = "HAN") 
                String arr,
            @RequestParam 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                @Parameter(description = "Flight date", example = "2025-12-01") 
                LocalDate date) {
        
        log.info("GET /api/flights/price-stats - {}-{} on {}", dep, arr, date);
        
        dep = dep.toUpperCase().trim();
        arr = arr.toUpperCase().trim();
        
        PriceStatistics stats = flightSearchService.getPriceStatistics(dep, arr, date);
        return ResponseEntity.ok(stats);
    }
}
