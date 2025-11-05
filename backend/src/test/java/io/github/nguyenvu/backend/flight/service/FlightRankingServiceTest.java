package io.github.nguyenvu.backend.flight.service;

import io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria;
import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import io.github.nguyenvu.backend.flight.entity.RouteStatsDaily;
import io.github.nguyenvu.backend.flight.repository.RouteStatsDailyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightRankingServiceTest {

    @Mock
    private RouteStatsDailyRepository routeStatsRepository;

    private FlightRankingService rankingService;

    @BeforeEach
    void setUp() {
        rankingService = new FlightRankingService(routeStatsRepository);
    }

    @Test
    void shouldRankCheaperFlightHigher() {
        // Given
        FlightSnapshot cheapFlight = createFlight("VN101", 1_000_000L, 10);
        FlightSnapshot expensiveFlight = createFlight("VN102", 2_000_000L, 10);
        
        RouteStatsDaily stats = new RouteStatsDaily();
        stats.setP50PriceCents(1_500_000L);
        stats.setP90PriceCents(2_500_000L);
        
        when(routeStatsRepository.findByRouteKeyAndDate(anyString(), any()))
            .thenReturn(Optional.of(stats));

        FlightSearchCriteria criteria = new FlightSearchCriteria();
        criteria.setDepIata("SGN");
        criteria.setArrIata("HAN");
        criteria.setSnapshotDate(LocalDate.now());

        // When
        List<FlightSnapshot> ranked = rankingService.rankFlights(
            List.of(expensiveFlight, cheapFlight), criteria);

        // Then
        assertThat(ranked).hasSize(2);
        assertThat(ranked.get(0).getFlightNo()).isEqualTo("VN101");
        assertThat(ranked.get(1).getFlightNo()).isEqualTo("VN102");
    }

    @Test
    void shouldRankShorterFlightHigher() {
        // Given
        FlightSnapshot shortFlight = createFlight("VN101", 1_500_000L, 8);  // Morning 8am
        FlightSnapshot longFlight = createFlight("VN102", 1_500_000L, 10);  // Morning 10am, longer duration
        
        // Make longFlight have longer duration
        longFlight.setArrTime(longFlight.getDepTime().plusHours(3));
        
        RouteStatsDaily stats = new RouteStatsDaily();
        stats.setP50PriceCents(1_500_000L);
        stats.setP90PriceCents(2_500_000L);
        
        when(routeStatsRepository.findByRouteKeyAndDate(anyString(), any()))
            .thenReturn(Optional.of(stats));

        FlightSearchCriteria criteria = new FlightSearchCriteria();
        criteria.setDepIata("SGN");
        criteria.setArrIata("HAN");
        criteria.setSnapshotDate(LocalDate.now());

        // When
        List<FlightSnapshot> ranked = rankingService.rankFlights(
            List.of(longFlight, shortFlight), criteria);

        // Then - shortFlight should be ranked higher due to shorter duration
        assertThat(ranked).hasSize(2);
        assertThat(ranked.get(0).getFlightNo()).isEqualTo("VN101");
    }

    @Test
    void shouldHandleEmptyFlightList() {
        // Given
        FlightSearchCriteria criteria = new FlightSearchCriteria();
        criteria.setDepIata("SGN");
        criteria.setArrIata("HAN");
        criteria.setSnapshotDate(LocalDate.now());

        // When
        List<FlightSnapshot> ranked = rankingService.rankFlights(List.of(), criteria);

        // Then
        assertThat(ranked).isEmpty();
    }

    @Test
    void shouldHandleNullFlightList() {
        // Given
        FlightSearchCriteria criteria = new FlightSearchCriteria();
        criteria.setDepIata("SGN");
        criteria.setArrIata("HAN");
        criteria.setSnapshotDate(LocalDate.now());

        // When
        List<FlightSnapshot> ranked = rankingService.rankFlights(null, criteria);

        // Then
        assertThat(ranked).isEmpty();
    }

    @Test
    void shouldRankMorningFlightsHigher() {
        // Given
        FlightSnapshot morningFlight = createFlight("VN101", 1_500_000L, 8);  // 8am
        FlightSnapshot nightFlight = createFlight("VN102", 1_500_000L, 23);   // 11pm
        
        RouteStatsDaily stats = new RouteStatsDaily();
        stats.setP50PriceCents(1_500_000L);
        stats.setP90PriceCents(2_500_000L);
        
        when(routeStatsRepository.findByRouteKeyAndDate(anyString(), any()))
            .thenReturn(Optional.of(stats));

        FlightSearchCriteria criteria = new FlightSearchCriteria();
        criteria.setDepIata("SGN");
        criteria.setArrIata("HAN");
        criteria.setSnapshotDate(LocalDate.now());

        // When
        List<FlightSnapshot> ranked = rankingService.rankFlights(
            List.of(nightFlight, morningFlight), criteria);

        // Then - morning flight should be ranked higher
        assertThat(ranked).hasSize(2);
        assertThat(ranked.get(0).getFlightNo()).isEqualTo("VN101");
    }

    private FlightSnapshot createFlight(String flightNo, Long priceCents, int departureHour) {
        FlightSnapshot flight = new FlightSnapshot();
        flight.setFlightNo(flightNo);
        flight.setPriceCents(priceCents);
        flight.setCarrier("VN");
        flight.setDepIata("SGN");
        flight.setArrIata("HAN");
        
        LocalDateTime depTime = LocalDateTime.now().plusDays(1).withHour(departureHour).withMinute(0);
        flight.setDepTime(depTime);
        flight.setArrTime(depTime.plusHours(2)); // Default 2 hour flight
        
        return flight;
    }
}
