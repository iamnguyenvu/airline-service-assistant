package io.github.nguyenvu.backend.flight.controller;

import io.github.nguyenvu.backend.flight.dto.CreateBookingRequest;
import io.github.nguyenvu.backend.flight.model.Booking;
import io.github.nguyenvu.backend.flight.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API for booking operations (mock implementation).
 */
@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Mock booking operations")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Create a mock booking.
     * 
     * POST /api/bookings
     */
    @PostMapping
    @Operation(
        summary = "Create mock booking",
        description = "Create a mock booking"
    )
    public ResponseEntity<Booking> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        
        log.info("POST /api/bookings - user: {}, flight: {}", 
            request.getUserId(), request.getFlightId());
        
        Booking booking = bookingService.createBooking(
            request.getUserId(),
            request.getFlightId(),
            request.getSeat(),
            request.getPrice(),
            request.getTravelDate()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    /**
     * Get booking by ID.
     * 
     * GET /api/bookings/{id}
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get booking by ID",
        description = "Retrieve a booking by its ID"
    )
    public ResponseEntity<Booking> getBooking(
            @PathVariable 
                @Parameter(description = "Booking ID") UUID id) {
        
        log.info("GET /api/bookings/{}", id);
        
        return bookingService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all bookings for a user.
     * 
     * GET /api/bookings?userId=user123
     */
    @GetMapping
    @Operation(
        summary = "Get user bookings",
        description = "Get all bookings for a specific user"
    )
    public ResponseEntity<List<Booking>> getUserBookings(
            @RequestParam 
                @Parameter(description = "User ID", required = true) String userId) {
        
        log.info("GET /api/bookings?userId={}", userId);
        
        List<Booking> bookings = bookingService.findByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Cancel a booking.
     * 
     * DELETE /api/bookings/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Cancel booking",
        description = "Cancel a booking by ID"
    )
    public ResponseEntity<Void> cancelBooking(
            @PathVariable 
                @Parameter(description = "Booking ID") UUID id) {
        
        log.info("DELETE /api/bookings/{}", id);
        
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}
