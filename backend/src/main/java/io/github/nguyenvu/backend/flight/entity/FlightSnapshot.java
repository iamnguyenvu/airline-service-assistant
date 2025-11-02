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
@Table(name = "flight_snapshot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "dep_iata", nullable = false, length = 3)
    private String depIata;

    @Column(name = "arr_iata", nullable = false, length = 3)
    private String arrIata;

    @Column(name = "dep_time", nullable = false)
    private LocalDateTime depTime;

    @Column(name = "arr_time", nullable = false)
    private LocalDateTime arrTime;

    @Column(name = "carrier", nullable = false, length = 3)
    private String carrier;

    @Column(name = "flight_no", nullable = false, length = 10)
    private String flightNo;

    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;

    @Column(name = "stops")
    private Short stops;

    @Column(name = "fare_family", length = 50)
    private String fareFamily;

    @Column(name = "baggage_kg", precision = 5, scale = 2)
    private BigDecimal baggageKg;

    @Column(name = "price_cents", nullable = false)
    private Long priceCents;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "source", length = 100)
    private String source;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}