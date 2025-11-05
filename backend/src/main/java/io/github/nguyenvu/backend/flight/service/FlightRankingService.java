package io.github.nguyenvu.backend.flight.service;

import io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria;
import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import io.github.nguyenvu.backend.flight.entity.RouteStatsDaily;
import io.github.nguyenvu.backend.flight.repository.RouteStatsDailyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service for ranking flights based on multiple factors.
 * 
 * Ranking factors with configurable weights:
 * - Price (40%): Lower is better, compared against P50/P90
 * - Duration (25%): Shorter is better
 * - CO₂ (15%): Lower is better, compared against median
 * - Time preference (10%): Match user's preferred departure time
 * - Carrier (10%): Match user's preferred carrier
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlightRankingService {

    private final RouteStatsDailyRepository routeStatsRepository;

    private static final double WEIGHT_PRICE = 0.40;
    private static final double WEIGHT_DURATION = 0.25;
    private static final double WEIGHT_CO2 = 0.15;
    private static final double WEIGHT_TIME_PREF = 0.10;
    private static final double WEIGHT_CARRIER = 0.10;

    /**
     * Rank flights by multiple factors.
     * Higher score = better flight.
     * 
     * @param flights List of flights to rank
     * @param criteria Search criteria (may contain preferences)
     * @return Ranked flights (best first)
     */
    public List<FlightSnapshot> rankFlights(
            List<FlightSnapshot> flights, 
            FlightSearchCriteria criteria) {
        
        if (flights == null || flights.isEmpty()) {
            return List.of();
        }

        String routeKey = buildRouteKey(
            criteria.getDepIata(), criteria.getArrIata());
        LocalDate date = criteria.getSnapshotDate() != null 
            ? criteria.getSnapshotDate() 
            : criteria.getStartDate();
        
        Optional<RouteStatsDaily> statsOpt = 
            routeStatsRepository.findByRouteKeyAndDate(routeKey, date);

        List<RankedFlight> rankedFlights = new ArrayList<>();
        for (FlightSnapshot flight : flights) {
            double score = calculateFlightScore(
                flight, criteria, statsOpt.orElse(null));
            rankedFlights.add(new RankedFlight(flight, score));
        }

        rankedFlights.sort(Comparator.comparingDouble(
            RankedFlight::score).reversed());

        return rankedFlights.stream()
            .map(RankedFlight::flight)
            .toList();
    }

    /**
     * Calculate composite score for a flight.
     * Score range: 0.0 (worst) to 100.0 (best)
     */
    private double calculateFlightScore(
            FlightSnapshot flight, 
            FlightSearchCriteria criteria, 
            RouteStatsDaily stats) {
        
        double priceScore = calculatePriceScore(flight, stats);
        double durationScore = calculateDurationScore(flight);
        double co2Score = calculateCO2Score(flight, stats);
        double timePrefScore = calculateTimePrefScore(flight, criteria);
        double carrierScore = calculateCarrierScore(flight, criteria);

        double totalScore = 
            (priceScore * WEIGHT_PRICE) +
            (durationScore * WEIGHT_DURATION) +
            (co2Score * WEIGHT_CO2) +
            (timePrefScore * WEIGHT_TIME_PREF) +
            (carrierScore * WEIGHT_CARRIER);

        log.debug("Flight {} score: price={}, duration={}, co2={}, time={}, carrier={}, total={}",
            flight.getFlightNo(), priceScore, durationScore, co2Score, 
            timePrefScore, carrierScore, totalScore);

        return totalScore;
    }

    /**
     * Price score: Compare against P50 and P90.
     * - Below P50: 90-100 points
     * - P50-P90: 60-90 points
     * - Above P90: 0-60 points
     */
    private double calculatePriceScore(FlightSnapshot flight, RouteStatsDaily stats) {
        if (stats == null || stats.getP50PriceCents() == null) {
            return 50.0; // Neutral if no historical data
        }

        long price = flight.getPriceCents();
        long p50 = stats.getP50PriceCents();
        long p90 = stats.getP90PriceCents() != null 
            ? stats.getP90PriceCents() 
            : p50 * 2;

        if (price <= p50) {
            double ratio = (double) price / p50;
            return 90.0 + (10.0 * (1.0 - ratio));
        } else if (price <= p90) {
            double ratio = (double) (price - p50) / (p90 - p50);
            return 60.0 + (30.0 * (1.0 - ratio));
        } else {
            double ratio = Math.min(2.0, (double) price / p90);
            return 60.0 * (2.0 - ratio);
        }
    }

    /**
     * Duration score: Shorter flights score higher.
     * - Under 1h: 100 points
     * - 1-2h: 80-100 points
     * - 2-4h: 60-80 points
     * - 4-8h: 30-60 points
     * - 8h+: 0-30 points
     */
    private double calculateDurationScore(FlightSnapshot flight) {
        if (flight.getDepTime() == null || flight.getArrTime() == null) {
            return 50.0;
        }

        long minutes = Duration.between(
            flight.getDepTime(), flight.getArrTime()).toMinutes();
        
        if (minutes < 60) return 100.0;
        if (minutes < 120) return 80.0 + (20.0 * (120 - minutes) / 60.0);
        if (minutes < 240) return 60.0 + (20.0 * (240 - minutes) / 120.0);
        if (minutes < 480) return 30.0 + (30.0 * (480 - minutes) / 240.0);
        return Math.max(0, 30.0 * (1440 - minutes) / 960.0);
    }

    /**
     * CO₂ score: Use avgCo2Kg as baseline.
     * Note: FlightSnapshot doesn't have CO2 field yet - returning neutral score.
     * TODO: Add CO2 calculation to FlightSnapshot based on duration.
     */
    private double calculateCO2Score(FlightSnapshot flight, RouteStatsDaily stats) {
        // CO2 not available in FlightSnapshot yet
        return 50.0; // Neutral score until CO2 data is added
    }

    /**
     * Time preference score.
     * TODO: Get preference from user profile.
     * Current: Business hours (6am-6pm) preferred.
     */
    private double calculateTimePrefScore(
            FlightSnapshot flight, FlightSearchCriteria criteria) {
        if (flight.getDepTime() == null) {
            return 50.0;
        }

        int hour = flight.getDepTime().getHour();

        if (hour >= 6 && hour < 12) return 100.0;  // Morning
        if (hour >= 12 && hour < 18) return 90.0;  // Afternoon
        if (hour >= 18 && hour < 22) return 70.0;  // Evening
        if (hour >= 22 || hour < 2) return 40.0;   // Night
        return 20.0;  // Red-eye (2-6am)
    }

    /**
     * Carrier preference score.
     * TODO: Integrate with user preferences.
     */
    private double calculateCarrierScore(
            FlightSnapshot flight, FlightSearchCriteria criteria) {
        if (criteria.getCarrier() != null && 
            criteria.getCarrier().equals(flight.getCarrier())) {
            return 100.0;
        }
        return 50.0; // Neutral
    }

    private String buildRouteKey(String depIata, String arrIata) {
        return depIata + "-" + arrIata;
    }

    private record RankedFlight(FlightSnapshot flight, double score) {}
}
