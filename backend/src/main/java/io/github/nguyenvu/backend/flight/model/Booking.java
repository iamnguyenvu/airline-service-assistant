package io.github.nguyenvu.backend.flight.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Booking {
    @EqualsAndHashCode.Include
    private UUID id;
    private String flightId;
    private String userId;
    private String seat;
    private BigDecimal price;
    private String status;
    private LocalDate travelDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
