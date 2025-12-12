package io.github.nguyenvu.backend.flight.service;

import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hybrid provider that combines AviationStack and Amadeus:
 * - Uses AviationStack for today's flights (real-time data, better for current day)
 * - Uses Amadeus for future flights (better availability and booking data)
 * - Falls back to the other provider if primary fails
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HybridFlightIngestionProvider implements FlightIngestionProvider {

    private final AviationstackFlightIngestionProvider aviationstackProvider;
    private final AmadeusFlightIngestionProvider amadeusProvider;

    @Override
    public String name() {
        return "hybrid";
    }

    @Override
    public List<FlightSnapshot> fetchDaily(LocalDate date) {
        LocalDate today = LocalDate.now();
        boolean isToday = date.equals(today);
        boolean isPast = date.isBefore(today);
        
        log.info("Hybrid provider fetching flights for date={} (today={}, past={})", date, isToday, isPast);
        
        List<FlightSnapshot> results = new ArrayList<>();
        
        if (isToday || isPast) {
            // For today or past dates: prioritize AviationStack (real-time data)
            log.info("Using AviationStack for today/past date: {}", date);
            try {
                List<FlightSnapshot> aviationstackResults = aviationstackProvider.fetchDaily(date);
                if (!aviationstackResults.isEmpty()) {
                    log.info("AviationStack returned {} flights for {}", aviationstackResults.size(), date);
                    results.addAll(aviationstackResults);
                } else {
                    log.warn("AviationStack returned no flights for {}, trying Amadeus as fallback", date);
                    // Fallback to Amadeus if AviationStack returns empty
                    try {
                        List<FlightSnapshot> amadeusResults = amadeusProvider.fetchDaily(date);
                        if (!amadeusResults.isEmpty()) {
                            log.info("Amadeus fallback returned {} flights for {}", amadeusResults.size(), date);
                            results.addAll(amadeusResults);
                        }
                    } catch (Exception e) {
                        log.warn("Amadeus fallback failed for {}: {}", date, e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.warn("AviationStack failed for {}, trying Amadeus as fallback: {}", date, e.getMessage());
                // Fallback to Amadeus if AviationStack fails
                try {
                    List<FlightSnapshot> amadeusResults = amadeusProvider.fetchDaily(date);
                    if (!amadeusResults.isEmpty()) {
                        log.info("Amadeus fallback returned {} flights for {}", amadeusResults.size(), date);
                        results.addAll(amadeusResults);
                    }
                } catch (Exception e2) {
                    log.error("Both providers failed for {}: AviationStack={}, Amadeus={}", 
                            date, e.getMessage(), e2.getMessage());
                }
            }
        } else {
            // For future dates: prioritize Amadeus (better booking/availability data)
            log.info("Using Amadeus for future date: {}", date);
            try {
                List<FlightSnapshot> amadeusResults = amadeusProvider.fetchDaily(date);
                if (!amadeusResults.isEmpty()) {
                    log.info("Amadeus returned {} flights for {}", amadeusResults.size(), date);
                    results.addAll(amadeusResults);
                } else {
                    log.warn("Amadeus returned no flights for {}, trying AviationStack as fallback", date);
                    // Fallback to AviationStack if Amadeus returns empty
                    try {
                        List<FlightSnapshot> aviationstackResults = aviationstackProvider.fetchDaily(date);
                        if (!aviationstackResults.isEmpty()) {
                            log.info("AviationStack fallback returned {} flights for {}", 
                                    aviationstackResults.size(), date);
                            results.addAll(aviationstackResults);
                        }
                    } catch (Exception e) {
                        log.warn("AviationStack fallback failed for {}: {}", date, e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.warn("Amadeus failed for {}, trying AviationStack as fallback: {}", date, e.getMessage());
                // Fallback to AviationStack if Amadeus fails
                try {
                    List<FlightSnapshot> aviationstackResults = aviationstackProvider.fetchDaily(date);
                    if (!aviationstackResults.isEmpty()) {
                        log.info("AviationStack fallback returned {} flights for {}", 
                                aviationstackResults.size(), date);
                        results.addAll(aviationstackResults);
                    }
                } catch (Exception e2) {
                    log.error("Both providers failed for {}: Amadeus={}, AviationStack={}", 
                            date, e.getMessage(), e2.getMessage());
                }
            }
        }
        
        // Deduplicate flights (same carrier, flightNo, depTime, arrTime)
        results = deduplicateFlights(results);
        
        log.info("Hybrid provider total results for {}: {} unique flights", date, results.size());
        return results;
    }

    @Override
    public List<FlightSnapshot> fetchRoute(LocalDate date, String depIata, String arrIata) {
        LocalDate today = LocalDate.now();
        boolean isToday = date.equals(today);
        boolean isPast = date.isBefore(today);
        
        log.info("Hybrid provider fetching route {}-{} for date={} (today={}, past={})", 
                depIata, arrIata, date, isToday, isPast);
        
        List<FlightSnapshot> results = new ArrayList<>();
        
        if (isToday || isPast) {
            // For today or past dates: prioritize AviationStack
            log.info("Using AviationStack for route {}-{} on today/past date: {}", depIata, arrIata, date);
            try {
                List<FlightSnapshot> aviationstackResults = aviationstackProvider.fetchRoute(date, depIata, arrIata);
                if (!aviationstackResults.isEmpty()) {
                    log.info("AviationStack returned {} flights for {}-{} on {}", 
                            aviationstackResults.size(), depIata, arrIata, date);
                    results.addAll(aviationstackResults);
                } else {
                    log.warn("AviationStack returned no flights for {}-{} on {}, trying Amadeus as fallback", 
                            depIata, arrIata, date);
                    try {
                        List<FlightSnapshot> amadeusResults = amadeusProvider.fetchRoute(date, depIata, arrIata);
                        if (!amadeusResults.isEmpty()) {
                            log.info("Amadeus fallback returned {} flights for {}-{} on {}", 
                                    amadeusResults.size(), depIata, arrIata, date);
                            results.addAll(amadeusResults);
                        }
                    } catch (Exception e) {
                        log.warn("Amadeus fallback failed for {}-{} on {}: {}", depIata, arrIata, date, e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.warn("AviationStack failed for {}-{} on {}, trying Amadeus as fallback: {}", 
                        depIata, arrIata, date, e.getMessage());
                try {
                    List<FlightSnapshot> amadeusResults = amadeusProvider.fetchRoute(date, depIata, arrIata);
                    if (!amadeusResults.isEmpty()) {
                        log.info("Amadeus fallback returned {} flights for {}-{} on {}", 
                                amadeusResults.size(), depIata, arrIata, date);
                        results.addAll(amadeusResults);
                    }
                } catch (Exception e2) {
                    log.error("Both providers failed for {}-{} on {}: AviationStack={}, Amadeus={}", 
                            depIata, arrIata, date, e.getMessage(), e2.getMessage());
                }
            }
        } else {
            // For future dates: prioritize Amadeus
            log.info("Using Amadeus for route {}-{} on future date: {}", depIata, arrIata, date);
            try {
                List<FlightSnapshot> amadeusResults = amadeusProvider.fetchRoute(date, depIata, arrIata);
                if (!amadeusResults.isEmpty()) {
                    log.info("Amadeus returned {} flights for {}-{} on {}", 
                            amadeusResults.size(), depIata, arrIata, date);
                    results.addAll(amadeusResults);
                } else {
                    log.warn("Amadeus returned no flights for {}-{} on {}, trying AviationStack as fallback", 
                            depIata, arrIata, date);
                    try {
                        List<FlightSnapshot> aviationstackResults = aviationstackProvider.fetchRoute(date, depIata, arrIata);
                        if (!aviationstackResults.isEmpty()) {
                            log.info("AviationStack fallback returned {} flights for {}-{} on {}", 
                                    aviationstackResults.size(), depIata, arrIata, date);
                            results.addAll(aviationstackResults);
                        }
                    } catch (Exception e) {
                        log.warn("AviationStack fallback failed for {}-{} on {}: {}", depIata, arrIata, date, e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.warn("Amadeus failed for {}-{} on {}, trying AviationStack as fallback: {}", 
                        depIata, arrIata, date, e.getMessage());
                try {
                    List<FlightSnapshot> aviationstackResults = aviationstackProvider.fetchRoute(date, depIata, arrIata);
                    if (!aviationstackResults.isEmpty()) {
                        log.info("AviationStack fallback returned {} flights for {}-{} on {}", 
                                aviationstackResults.size(), depIata, arrIata, date);
                        results.addAll(aviationstackResults);
                    }
                } catch (Exception e2) {
                    log.error("Both providers failed for {}-{} on {}: Amadeus={}, AviationStack={}", 
                            depIata, arrIata, date, e.getMessage(), e2.getMessage());
                }
            }
        }
        
        // Deduplicate flights
        results = deduplicateFlights(results);
        
        log.info("Hybrid provider total results for {}-{} on {}: {} unique flights", 
                depIata, arrIata, date, results.size());
        return results;
    }

    /**
     * Deduplicate flights based on carrier, flightNo, depTime, and arrTime
     */
    private List<FlightSnapshot> deduplicateFlights(List<FlightSnapshot> flights) {
        if (flights == null || flights.isEmpty()) {
            return flights;
        }
        
        Set<String> seen = new HashSet<>();
        List<FlightSnapshot> unique = new ArrayList<>();
        
        for (FlightSnapshot flight : flights) {
            // Create a unique key: carrier + flightNo + depTime + arrTime
            String key = String.format("%s|%s|%s|%s",
                    flight.getCarrier() != null ? flight.getCarrier() : "",
                    flight.getFlightNo() != null ? flight.getFlightNo() : "",
                    flight.getDepTime() != null ? flight.getDepTime().toString() : "",
                    flight.getArrTime() != null ? flight.getArrTime().toString() : "");
            
            if (!seen.contains(key)) {
                seen.add(key);
                unique.add(flight);
            }
        }
        
        return unique;
    }
}



