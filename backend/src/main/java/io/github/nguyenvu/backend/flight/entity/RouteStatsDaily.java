package io.github.nguyenvu.backend.flight.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_stats_daily", uniqueConstraints = @UniqueConstraint(columnNames = {"route_key", "date"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteStatsDaily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_key", nullable = false, length = 10)
    private String routeKey;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "min_price_cents")
    private Long minPriceCents;

    @Column(name = "p50_price_cents")
    private Long p50PriceCents;

    @Column(name = "p90_price_cents")
    private Long p90PriceCents;

    @Column(name = "avg_duration_min")
    private Integer avgDurationMin;

    @Column(name = "avg_co2_kg", precision = 10, scale = 2)
    private BigDecimal avgCo2Kg;

    @Column(name = "flight_count")
    private Integer flightCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
