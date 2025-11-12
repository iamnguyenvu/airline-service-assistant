package io.github.nguyenvu.backend.flight.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@lombok.Builder
@lombok.AllArgsConstructor
public class FlightSearchCriteria {
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "depIata must be 3 letters (IATA)")
    private String depIata;

    @Pattern(regexp = "^[A-Za-z]{3}$", message = "arrIata must be 3 letters (IATA)")
    private String arrIata;

    private LocalDate snapshotDate;
    private LocalDate startDate;
    private LocalDate endDate;

    @Pattern(regexp = "^[A-Za-z0-9\\-\\s]{0,50}$", message = "carrier invalid")
    private String carrier;

    @PositiveOrZero(message = "maxPriceCents must be >= 0")
    private Long maxPriceCents;

    @PositiveOrZero(message = "minPriceCents must be >= 0")
    private Long minPriceCents;
}
