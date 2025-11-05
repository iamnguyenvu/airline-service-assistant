package io.github.nguyenvu.backend.flight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateBookingRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Flight ID is required")
    private String flightId;
    
    private String seat;
    
    @NotNull(message = "Price is required")
    private BigDecimal price;
    
    @NotNull(message = "Travel date is required")
    private LocalDate travelDate;
}
