package io.github.nguyenvu.backend.flight.service;

import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;

import java.time.LocalDate;
import java.util.List;

public interface FlightIngestionProvider {
    String name();
    List<FlightSnapshot> fetchDaily(LocalDate date);

    default List<FlightSnapshot> fetchRoute(LocalDate date, String depIata, String arrIata) {
        return fetchDaily(date).stream()
                .filter(f -> depIata.equalsIgnoreCase(f.getDepIata()) && arrIata.equalsIgnoreCase(f.getArrIata()))
                .toList();
    }
}


