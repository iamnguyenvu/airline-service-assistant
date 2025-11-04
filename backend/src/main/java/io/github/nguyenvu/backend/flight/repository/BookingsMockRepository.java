package io.github.nguyenvu.backend.flight.repository;

import io.github.nguyenvu.backend.flight.model.Booking;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory mock repository for Booking entity.
 * Thread-safe. For dev/testing only (data lost on restart).
 * Replace with JPA repository in production.
 */
@Repository
public class BookingsMockRepository {
    private final Map<UUID, Booking> store = new ConcurrentHashMap<>();

    /** Save or update booking */
    public Booking save(Booking booking) {
        if (booking == null) throw new IllegalArgumentException("Booking cannot be null");
        
        UUID id = booking.getId();
        if (id == null) {
            id = UUID.randomUUID();
            booking.setId(id);
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (booking.getCreatedAt() == null) booking.setCreatedAt(now);
        booking.setUpdatedAt(now);
        
        store.put(id, booking);
        return booking;
    }

    /** Find booking by ID */
    public Optional<Booking> findById(UUID id) {
        return Optional.ofNullable(id).map(store::get);
    }

    /** Find all bookings */
    public List<Booking> findAll() {
        return new ArrayList<>(store.values());
    }

    /** Find bookings by user ID */
    public List<Booking> findByUserId(String userId) {
        if (userId == null) return List.of();
        return store.values().stream()
                .filter(b -> userId.equals(b.getUserId()))
                .toList();
    }

    /** Find bookings by flight ID */
    public List<Booking> findByFlightId(String flightId) {
        if (flightId == null) return List.of();
        return store.values().stream()
                .filter(b -> flightId.equals(b.getFlightId()))
                .toList();
    }

    /** Check if booking exists */
    public boolean existsById(UUID id) {
        return id != null && store.containsKey(id);
    }

    /** Count all bookings */
    public long count() {
        return store.size();
    }

    /** Delete booking by ID */
    public void deleteById(UUID id) {
        if (id != null) store.remove(id);
    }

    /** Delete all bookings */
    public void deleteAll() {
        store.clear();
    }
}
