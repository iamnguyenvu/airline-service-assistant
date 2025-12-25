package io.github.nguyenvu.backend.flight.service;

import io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria;
import io.github.nguyenvu.backend.flight.dto.FlightSearchResult;
import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import io.github.nguyenvu.backend.flight.repository.FlightSnapshotRepository;
import io.github.nguyenvu.backend.flight.repository.PriceStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service for flight search operations.
 * Provides intelligent flight search with ranking, filtering, and caching.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightSearchService {

    private final FlightSnapshotRepository flightSnapshotRepository;
    private final FlightRankingService rankingService;
    private final FlightIngestionService flightIngestionService;

    @Value("${app.ingestion.provider:hybrid}")
    private String ingestionProvider;

    // Self-injection with @Lazy to avoid circular dependency
    @Lazy
    @Autowired
    private FlightSearchService self;

    /**
     * Search flights with criteria and intelligent ranking.
     * Results are cached for 5 minutes.
     * 
     * @param criteria Search criteria
     * @param page     Page number (0-indexed)
     * @param size     Page size
     * @return Paginated and ranked flight search results
     */
    @Cacheable(value = "flightSearch", key = "#criteria.toString() + '-' + #page + '-' + #size", unless = "#result == null || #result.flights.isEmpty()")
    public FlightSearchResult search(FlightSearchCriteria criteria, int page, int size) {
        log.info("Searching flights: {}", criteria);

        validateSearchCriteria(criteria);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("priceCents").ascending());

        Page<FlightSnapshot> flightPage = flightSnapshotRepository.search(criteria, pageable);

        List<FlightSnapshot> rankedFlights = rankingService.rankFlights(flightPage.getContent(), criteria);

        return FlightSearchResult.builder()
                .flights(rankedFlights)
                .totalElements(flightPage.getTotalElements())
                .totalPages(flightPage.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .hasNext(flightPage.hasNext())
                .hasPrevious(flightPage.hasPrevious())
                .searchCriteria(criteria)
                .build();
    }

    /**
     * Find cheapest flights for a route and date.
     */
    @Cacheable(value = "cheapestFlights", key = "#depIata + '-' + #arrIata + '-' + #date + '-' + #limit")
    public List<FlightSnapshot> findCheapestFlights(
            String depIata, String arrIata, LocalDate date, int limit) {

        log.info("Finding {} cheapest flights: {}-{} on {}",
                limit, depIata, arrIata, date);

        if (limit <= 5) {
            return flightSnapshotRepository
                    .findTop5ByDepIataAndArrIataAndSnapshotDateOrderByPriceCentsAsc(
                            depIata, arrIata, date)
                    .stream()
                    .limit(limit)
                    .toList();
        }

        return flightSnapshotRepository
                .findByDepIataAndArrIataAndSnapshotDateOrderByPriceCentsAsc(
                        depIata, arrIata, date)
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * Get available carriers for a route.
     */
    @Cacheable(value = "routeCarriers", key = "#depIata + '-' + #arrIata")
    public List<String> getAvailableCarriers(String depIata, String arrIata) {
        return flightSnapshotRepository.findDistinctCarriersByRoute(
                depIata, arrIata);
    }

    /**
     * Check if flights are available.
     */
    public boolean hasFlights(String depIata, String arrIata, LocalDate date) {
        return flightSnapshotRepository.existsByDepIataAndArrIataAndSnapshotDate(
                depIata, arrIata, date);
    }

    /**
     * Get price statistics for a route and date.
     */
    @Cacheable(value = "priceStats", key = "#depIata + '-' + #arrIata + '-' + #date")
    public PriceStatistics getPriceStatistics(
            String depIata, String arrIata, LocalDate date) {
        return flightSnapshotRepository.getPriceStatistics(
                depIata, arrIata, date);
    }

    /**
     * Get flights for today, prioritized: domestic Vietnam flights first, then
     * Vietnam to international.
     * If no flights found in database, automatically fetches from API and saves to
     * database.
     * Results are cached by time slot (4-hour intervals) to ensure data freshness
     * throughout the day.
     * This means cache refreshes roughly every 4 hours, so morning data won't be
     * stale by evening.
     * 
     * @param limit Maximum number of flights to return
     * @return FlightSearchResult with prioritized flights
     */
    @Cacheable(value = "todayFlights", key = "'today-' + T(java.time.LocalDate).now().toString() + '-' + T(io.github.nguyenvu.backend.flight.service.FlightSearchService).getTimeSlot() + '-' + #limit", unless = "#result == null || #result.flights.isEmpty()")
    public FlightSearchResult getTodayFlights(int limit) {
        LocalDate today = LocalDate.now();
        log.info("Getting today's flights (limit: {}) for date: {}", limit, today);

        // Priority 1: Domestic Vietnam flights
        List<FlightSnapshot> domesticFlights = flightSnapshotRepository.findTodayDomesticFlights(today);
        log.debug("Found {} domestic flights for {}", domesticFlights.size(), today);

        // Priority 2: Vietnam to international flights
        List<FlightSnapshot> internationalFlights = flightSnapshotRepository.findTodayInternationalFlights(today);
        log.debug("Found {} international flights for {}", internationalFlights.size(), today);

        // If no flights for today in database, fetch from API
        if (domesticFlights.isEmpty() && internationalFlights.isEmpty()) {
            log.warn("No flights found in database for today ({}). Fetching from API...", today);
            try {
                // Call via self-injected proxy to ensure transaction is created
                self.fetchAndSaveTodayFlights(today);
                log.info("Successfully fetched flights from API for today ({}). Querying database again...", today);

                // Query database again after fetching
                domesticFlights = flightSnapshotRepository.findTodayDomesticFlights(today);
                internationalFlights = flightSnapshotRepository.findTodayInternationalFlights(today);
                log.info("After fetching from API, found {} domestic and {} international flights for today",
                        domesticFlights.size(), internationalFlights.size());
            } catch (Exception e) {
                log.error("Failed to fetch flights from API for today ({}): {}", today, e.getMessage(), e);
                // Continue with fallback logic below
            }
        }

        // If still no flights for today, try to get flights from nearby dates
        // (yesterday, tomorrow) as fallback
        if (domesticFlights.isEmpty() && internationalFlights.isEmpty()) {
            log.warn("Still no flights found for today ({}). Checking nearby dates...", today);
            // Try tomorrow (T+1) - ingestion usually runs for T+1
            LocalDate tomorrow = today.plusDays(1);
            domesticFlights = flightSnapshotRepository.findTodayDomesticFlights(tomorrow);
            internationalFlights = flightSnapshotRepository.findTodayInternationalFlights(tomorrow);
            log.info("Found {} domestic and {} international flights for tomorrow ({})",
                    domesticFlights.size(), internationalFlights.size(), tomorrow);
        }

        // Combine and limit
        List<FlightSnapshot> allFlights = new java.util.ArrayList<>();
        allFlights.addAll(domesticFlights);
        allFlights.addAll(internationalFlights);

        // Limit to requested number
        List<FlightSnapshot> limitedFlights = allFlights.stream()
                .limit(limit)
                .toList();

        log.info("Returning {} flights ({} domestic, {} international) for date: {}",
                limitedFlights.size(), domesticFlights.size(), internationalFlights.size(), today);

        return FlightSearchResult.builder()
                .flights(limitedFlights)
                .totalElements((long) allFlights.size())
                .totalPages(1)
                .currentPage(0)
                .pageSize(limit)
                .hasNext(allFlights.size() > limit)
                .hasPrevious(false)
                .searchCriteria(FlightSearchCriteria.builder()
                        .snapshotDate(today)
                        .build())
                .build();
    }

    /**
     * Fetch flights for a specific date from API and save to database.
     * This method has write transaction access with REQUIRES_NEW propagation
     * to ensure it runs in a new transaction even when called from read-only
     * context.
     * 
     * @param date Date to fetch flights for
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @CacheEvict(value = "todayFlights", allEntries = true)
    public void fetchAndSaveTodayFlights(LocalDate date) {
        log.info("Fetching flights from API for date: {} using provider: {}", date, ingestionProvider);
        try {
            List<FlightSnapshot> fetchedFlights = flightIngestionService.testFetch(ingestionProvider, date, null, null);
            if (fetchedFlights == null || fetchedFlights.isEmpty()) {
                log.warn("No flights fetched from API for date: {}", date);
                return;
            }

            log.info("Fetched {} flights from API for date: {}. Saving to database...", fetchedFlights.size(), date);
            flightSnapshotRepository.saveAll(fetchedFlights);
            log.info("Successfully saved {} flights to database for date: {}", fetchedFlights.size(), date);
        } catch (Exception e) {
            log.error("Error fetching and saving flights for date {}: {}", date, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch flights from API for date " + date + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get current time slot (4-hour intervals: 0-3, 4-7, 8-11, 12-15, 16-19,
     * 20-23).
     * This ensures cache refreshes every 4 hours throughout the day.
     * 
     * @return Time slot identifier (0-5)
     */
    public static int getTimeSlot() {
        int hour = LocalTime.now().getHour();
        return hour / 4; // 0-3: slot 0, 4-7: slot 1, 8-11: slot 2, 12-15: slot 3, 16-19: slot 4, 20-23:
                         // slot 5
    }

    /**
     * Validate search criteria.
     */
    private void validateSearchCriteria(FlightSearchCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Search criteria cannot be null");
        }

        // At least one airport (departure or arrival) must be specified
        boolean hasDep = criteria.getDepIata() != null && !criteria.getDepIata().trim().isEmpty();
        boolean hasArr = criteria.getArrIata() != null && !criteria.getArrIata().trim().isEmpty();

        if (!hasDep && !hasArr) {
            throw new IllegalArgumentException("At least one airport (departure or arrival) is required");
        }

        if (criteria.getSnapshotDate() == null && criteria.getStartDate() == null) {
            throw new IllegalArgumentException("Flight date required");
        }

        if (criteria.getStartDate() != null && criteria.getEndDate() != null) {
            if (criteria.getStartDate().isAfter(criteria.getEndDate())) {
                throw new IllegalArgumentException(
                        "Start date must be before end date");
            }
        }

        if (criteria.getMinPriceCents() != null &&
                criteria.getMaxPriceCents() != null) {
            if (criteria.getMinPriceCents() > criteria.getMaxPriceCents()) {
                throw new IllegalArgumentException(
                        "Min price must be <= max price");
            }
        }
    }
}
