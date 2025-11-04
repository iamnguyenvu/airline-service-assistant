package io.github.nguyenvu.backend.flight.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class FlightSearchCriteria {
    private String depIata;
    private String arrIata;
    private LocalDate snapshotDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String carrier;
    private Long maxPriceCents;
    private Long minPriceCents;
}
