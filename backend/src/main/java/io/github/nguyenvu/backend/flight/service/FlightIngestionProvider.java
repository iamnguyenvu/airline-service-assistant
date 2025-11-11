package io.github.nguyenvu.backend.flight.service;

import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;

import java.time.LocalDate;
import java.util.List;

public interface FlightIngestionProvider {
    String name();
    List<FlightSnapshot> fetchDaily(LocalDate date);
}


