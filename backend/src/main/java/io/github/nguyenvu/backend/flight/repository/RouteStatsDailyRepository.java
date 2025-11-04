package io.github.nguyenvu.backend.flight.repository;

import io.github.nguyenvu.backend.flight.entity.RouteStatsDaily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link RouteStatsDaily} — daily aggregated route statistics.
 *
 * Provides common queries used by analytics and reporting flows.
 */
public interface RouteStatsDailyRepository extends JpaRepository<RouteStatsDaily, Long> {

    /** Find stats by route key. */
    List<RouteStatsDaily> findByRouteKey(String routeKey);

    /** Find stats by route key with pagination. */
    Page<RouteStatsDaily> findByRouteKey(String routeKey, Pageable pageable);

    /** Find stats for a specific route key and date. */
    Optional<RouteStatsDaily> findByRouteKeyAndDate(String routeKey, LocalDate date);

    /** Find stats within a date range (inclusive). */
    List<RouteStatsDaily> findByDateBetween(LocalDate start, LocalDate end);

    /** Count stats entries for a route on a specific date. */
    long countByRouteKeyAndDate(String routeKey, LocalDate date);

    /** Delete stats entries older than the specified date. Returns number of rows deleted. */
    int deleteByDateBefore(LocalDate cutoffDate);
}
