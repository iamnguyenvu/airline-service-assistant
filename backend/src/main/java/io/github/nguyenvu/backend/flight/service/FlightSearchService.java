package io.github.nguyenvu.backend.flight.service;

import io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria;
import io.github.nguyenvu.backend.flight.dto.FlightSearchResult;
import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import io.github.nguyenvu.backend.flight.repository.FlightSnapshotRepository;
import io.github.nguyenvu.backend.flight.repository.PriceStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    /**
     * Search flights with criteria and intelligent ranking.
     * Results are cached for 5 minutes.
     * 
     * @param criteria Search criteria
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated and ranked flight search results
     */
    @Cacheable(value = "flightSearch", 
               key = "#criteria.toString() + '-' + #page + '-' + #size",
               unless = "#result == null || #result.flights.isEmpty()")
    public FlightSearchResult search(FlightSearchCriteria criteria, int page, int size) {
        log.info("Searching flights: {}", criteria);
        
        validateSearchCriteria(criteria);
        
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by("priceCents").ascending());
        
        Page<FlightSnapshot> flightPage = 
            flightSnapshotRepository.search(criteria, pageable);
        
        List<FlightSnapshot> rankedFlights = 
            rankingService.rankFlights(flightPage.getContent(), criteria);
        
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
    @Cacheable(value = "cheapestFlights", 
               key = "#depIata + '-' + #arrIata + '-' + #date + '-' + #limit")
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
    @Cacheable(value = "priceStats", 
               key = "#depIata + '-' + #arrIata + '-' + #date")
    public PriceStatistics getPriceStatistics(
            String depIata, String arrIata, LocalDate date) {
        return flightSnapshotRepository.getPriceStatistics(
            depIata, arrIata, date);
    }

    /**
     * Get flights for today, prioritized: domestic Vietnam flights first, then Vietnam to international.
     * Results are cached for 30 minutes to avoid excessive API calls.
     * 
     * @param limit Maximum number of flights to return
     * @return FlightSearchResult with prioritized flights
     */
    @Cacheable(value = "todayFlights", 
               key = "'today-' + T(java.time.LocalDate).now().toString() + '-' + #limit",
               unless = "#result == null || #result.flights.isEmpty()")
    public FlightSearchResult getTodayFlights(int limit) {
        LocalDate today = LocalDate.now();
        log.info("Getting today's flights (limit: {})", limit);
        
        // Priority 1: Domestic Vietnam flights
        List<FlightSnapshot> domesticFlights = flightSnapshotRepository.findTodayDomesticFlights(today);
        
        // Priority 2: Vietnam to international flights
        List<FlightSnapshot> internationalFlights = flightSnapshotRepository.findTodayInternationalFlights(today);
        
        // Combine and limit
        List<FlightSnapshot> allFlights = new java.util.ArrayList<>();
        allFlights.addAll(domesticFlights);
        allFlights.addAll(internationalFlights);
        
        // Limit to requested number
        List<FlightSnapshot> limitedFlights = allFlights.stream()
            .limit(limit)
            .toList();
        
        log.info("Found {} domestic and {} international flights for today (returning {})", 
            domesticFlights.size(), internationalFlights.size(), limitedFlights.size());
        
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
