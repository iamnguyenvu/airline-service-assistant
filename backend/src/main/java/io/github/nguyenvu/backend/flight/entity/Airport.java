package io.github.nguyenvu.backend.flight.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Airport master data entity.
 * Replaces hardcoded airport lists for flexible airport management.
 */
@Entity
@Table(name = "airports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airport {

    @Id
    @Column(name = "iata_code", length = 3)
    private String iataCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "airport_type", nullable = false, length = 20)
    @Builder.Default
    private AirportType airportType = AirportType.DOMESTIC;

    @Builder.Default
    private Boolean active = true;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 50)
    @Builder.Default
    private String timezone = "Asia/Ho_Chi_Minh";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum AirportType {
        DOMESTIC,
        INTERNATIONAL
    }

    /**
     * Check if this is a Vietnam domestic airport.
     */
    public boolean isVietnamDomestic() {
        return "Vietnam".equals(country) && airportType == AirportType.DOMESTIC;
    }
}
