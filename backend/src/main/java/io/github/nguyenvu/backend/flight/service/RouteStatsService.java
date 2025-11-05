package io.github.nguyenvu.backend.flight.service;

import io.github.nguyenvu.backend.flight.entity.RouteStatsDaily;
import io.github.nguyenvu.backend.flight.repository.RouteStatsDailyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for route statistics and analytics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteStatsService {

    private final RouteStatsDailyRepository routeStatsRepository;

    /**
     * Get stats for a specific route and date.
     */
    @Cacheable(value = "routeStats", key = "#routeKey + '-' + #date")
    public Optional<RouteStatsDaily> getStats(String routeKey, LocalDate date) {
        return routeStatsRepository.findByRouteKeyAndDate(routeKey, date);
    }

    /**
     * Get historical stats for a route.
     */
    public List<RouteStatsDaily> getHistoricalStats(
            String routeKey, LocalDate from, LocalDate to) {
        return routeStatsRepository.findByDateBetween(from, to).stream()
            .filter(stats -> stats.getRouteKey().equals(routeKey))
            .toList();
    }

    /**
     * Build route key from airport codes.
     */
    public String buildRouteKey(String depIata, String arrIata) {
        return depIata + "-" + arrIata;
    }
}
