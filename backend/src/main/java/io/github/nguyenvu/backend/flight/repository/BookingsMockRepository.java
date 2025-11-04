package io.github.nguyenvu.backend.flight.repository;

import io.github.nguyenvu.backend.flight.model.Booking;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class BookingsMockRepository {
    private final ConcurrentMap<UUID, Booking> bookingConcurrentMap = new ConcurrentHashMap<>();
}
