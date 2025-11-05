package io.github.nguyenvu.backend.flight.service;

import io.github.nguyenvu.backend.flight.model.Booking;
import io.github.nguyenvu.backend.flight.repository.BookingsMockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for booking operations (mock implementation).
 * In production, integrate with real booking system.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingsMockRepository bookingsRepository;

    /**
     * Create a mock booking.
     * 
     * @param userId User ID
     * @param flightId Flight ID or flight number
     * @param seat Seat number (optional)
     * @param price Booking price
     * @param travelDate Travel date
     * @return Created booking
     */
    public Booking createBooking(
            String userId, 
            String flightId,
            String seat,
            java.math.BigDecimal price,
            java.time.LocalDate travelDate) {
        
        log.info("Creating booking: user={}, flight={}", userId, flightId);
        
        Booking booking = Booking.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .flightId(flightId)
            .seat(seat)
            .price(price)
            .travelDate(travelDate)
            .status("CONFIRMED")
            .createdAt(LocalDateTime.now())
            .build();
        
        return bookingsRepository.save(booking);
    }

    /**
     * Find booking by ID.
     */
    public Optional<Booking> findById(UUID id) {
        return bookingsRepository.findById(id);
    }

    /**
     * Find all bookings for a user.
     */
    public List<Booking> findByUserId(String userId) {
        return bookingsRepository.findByUserId(userId);
    }

    /**
     * Cancel a booking.
     */
    public void cancelBooking(UUID id) {
        Optional<Booking> bookingOpt = bookingsRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus("CANCELLED");
            booking.setUpdatedAt(LocalDateTime.now());
            bookingsRepository.save(booking);
            log.info("Cancelled booking: {}", id);
        }
    }

}
